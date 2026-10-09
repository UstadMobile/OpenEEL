package world.respect.shared.viewmodel.enrollment.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.dataloadstate.ext.isReadyAndSettled
import world.respect.datalayer.school.model.Enrollment
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.libutil.util.time.localDateInCurrentTimeZone
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.school.SchoolPrimaryKeyGenerator
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.date_later
import world.respect.shared.generated.resources.edit_enrollment
import world.respect.shared.generated.resources.save
import world.respect.shared.navigation.EnrollmentEdit
import world.respect.shared.navigation.EnrollmentList
import world.respect.shared.navigation.NavCommand
import world.respect.shared.resources.UiText
import world.respect.shared.util.LaunchDebouncer
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState
import kotlin.getValue
import kotlin.time.Clock

data class EnrollmentEditUiState(
    val enrollment: DataLoadState<Enrollment> = DataLoadingState(),
    val beginDateError: UiText? = null,
) {
    val fieldsEnabled: Boolean
        get() = enrollment.isReadyAndSettled()

    val hasErrors: Boolean
        get() = beginDateError != null
}

class EnrollmentEditViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
    private val json: Json,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    val route: EnrollmentEdit = savedStateHandle.toRoute()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(EnrollmentEditUiState())

    val uiState = _uiState.asStateFlow()

    private val debouncer = LaunchDebouncer(viewModelScope)

    private val schoolPrimaryKeyGenerator: SchoolPrimaryKeyGenerator by inject()

    private val uid = route.uid ?: schoolPrimaryKeyGenerator.primaryKeyGenerator.nextId(
        Enrollment.TABLE_ID
    ).toString()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.edit_enrollment.asUiText(),
                actionBarButtonState = ActionBarButtonUiState(
                    onClick = ::onClickSave,
                    text = Res.string.save.asUiText(),
                    visible = true,
                ),
                hideBottomNavigation = true,
            )
        }

        launchWithLoadingIndicator {
            if (route.uid != null) {
                loadEntity(
                    json = json,
                    serializer = Enrollment.serializer(),
                    loadFn = { params ->
                        schoolDataSource.enrollmentDataSource.findByGuid(
                            loadParams = params,
                            guid = route.uid
                        )
                    },
                    uiUpdateFn = { enrollment ->
                        _uiState.update {
                            it.copy(enrollment = enrollment)
                        }
                    }
                )
            } else {
                _uiState.update { prev ->
                    prev.copy(
                        enrollment = DataReadyState(
                            Enrollment(
                                uid = uid,
                                personUid = route.personGuid,
                                classUid = route.clazzGuid,
                                role = EnrollmentRoleEnum.valueOf(route.role)
                            )
                        )
                    )
                }
            }
        }
    }

    fun onEntityChanged(enrollment: Enrollment) {
        val enrollmentToCommit = _uiState.updateAndGet { prev ->
            val prevEnrollment = prev.enrollment.dataOrNull()

            prev.copy(
                enrollment = DataReadyState(enrollment),
                beginDateError = prev.beginDateError?.takeIf {
                    enrollment.beginDate == prevEnrollment?.beginDate
                },
            )
        }.enrollment.dataOrNull() ?: return

        debouncer.launch(DEFAULT_SAVED_STATE_KEY) {
            savedStateHandle[DEFAULT_SAVED_STATE_KEY] = json.encodeToString(enrollmentToCommit)
        }
    }

    fun onClickSave() {
        val currentEnrollment = _uiState.value.enrollment.dataOrNull() ?: return

        val endDate = currentEnrollment.endDate

        val enrollment = currentEnrollment.copy(
            lastModified = Clock.System.now(),
            removedAt = if(endDate == null || endDate > localDateInCurrentTimeZone()) {
                null
            }else {
                currentEnrollment.removedAt
            }
        )

        val beginDate = enrollment.beginDate

        _uiState.update { prev ->
            prev.copy(
                beginDateError = if (beginDate != null && endDate != null && beginDate > endDate) {
                    Res.string.date_later.asUiText()
                } else {
                    null
                },
            )
        }

        if (uiState.value.hasErrors)
            return

        launchWithLoadingIndicator {
            try {
                schoolDataSource.enrollmentDataSource.store(listOf(enrollment))

                if (route.uid == null) {
                    _navCommandFlow.tryEmit(
                        NavCommand.Navigate(
                            EnrollmentList(route.personGuid, route.role, route.clazzGuid),
                            popUpTo = route,
                            popUpToInclusive = true
                        )
                    )
                } else {
                    _navCommandFlow.tryEmit(NavCommand.PopUp())
                }
            } catch (e: Throwable) {
                //needs to display snack bar here
                e.printStackTrace()
            }
        }
    }
}