package org.openeel.shared.viewmodel.clazz.edit

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
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataLoadingState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.datalayer.SchoolDataSource
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.dataloadstate.ext.isReadyAndSettled
import org.openeel.datalayer.school.model.Clazz
import org.openeel.datalayer.school.model.Clazz.Companion.DEFAULT_INVITE_CODE_LEN
import org.openeel.datalayer.school.model.Clazz.Companion.DEFAULT_INVITE_CODE_MAX
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.domain.createclass.CreateClassUseCase
import org.openeel.shared.domain.school.SchoolPrimaryKeyGenerator
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.add_clazz
import org.openeel.shared.generated.resources.edit_clazz
import org.openeel.shared.generated.resources.required_field
import org.openeel.shared.generated.resources.save
import org.openeel.shared.navigation.ClazzDetail
import org.openeel.shared.navigation.ClazzEdit
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.resources.UiText
import org.openeel.shared.util.LaunchDebouncer
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel
import org.openeel.shared.viewmodel.app.appstate.ActionBarButtonUiState
import kotlin.random.Random
import kotlin.time.Clock

data class ClazzEditUiState(
    val clazzNameError: UiText? = null,
    val clazz: DataLoadState<Clazz> = DataLoadingState(),
) {
    val fieldsEnabled: Boolean
        get() = clazz.isReadyAndSettled()
}

class ClazzEditViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: AppAccountManager,
    private val json: Json,
) : OpenEelViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()
    private val route: ClazzEdit = savedStateHandle.toRoute()

    private val schoolPrimaryKeyGenerator: SchoolPrimaryKeyGenerator by inject()

    private val createClassUseCase: CreateClassUseCase by inject()

    private val guid = route.guid ?: schoolPrimaryKeyGenerator.primaryKeyGenerator.nextId(
        Clazz.TABLE_ID
    ).toString()

    private val _uiState = MutableStateFlow(ClazzEditUiState())

    val uiState = _uiState.asStateFlow()

    private val debouncer = LaunchDebouncer(viewModelScope)

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = if (route.guid == null) {
                    Res.string.add_clazz.asUiText()
                } else {
                    Res.string.edit_clazz.asUiText()
                },
                userAccountIconVisible = false,
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = Res.string.save.asUiText(),
                    onClick = ::onClickSave
                ),
                hideBottomNavigation = true,
            )
        }

        launchWithLoadingIndicator {
            if (route.guid != null) {
                loadEntity(
                    json = json,
                    serializer = Clazz.serializer(),
                    loadFn = { params ->
                        schoolDataSource.classDataSource.findByGuid(params, guid)
                    },
                    uiUpdateFn = { clazz ->
                        _uiState.update { prev -> prev.copy(clazz = clazz) }
                    }
                )
            } else {
                _uiState.update { prev ->
                    prev.copy(
                        clazz = DataReadyState(
                            Clazz(
                                guid = guid,
                                title = "",
                                description = "",
                            )
                        )
                    )
                }
            }
        }
    }

    fun onEntityChanged(clazz: Clazz) {
        val classToCommit = _uiState.updateAndGet { prev ->
            prev.copy(clazz = DataReadyState(clazz))
        }.clazz.dataOrNull() ?: return

        debouncer.launch(DEFAULT_SAVED_STATE_KEY) {
            savedStateHandle[DEFAULT_SAVED_STATE_KEY] = json.encodeToString(classToCommit)
        }
    }

    fun onClickSave() {
        val clazz = _uiState.value.clazz.dataOrNull()?.copy(
            lastModified = Clock.System.now()
        ) ?: return

        if (clazz.title.isBlank()) {
            _uiState.update { prev ->
                prev.copy(clazzNameError = Res.string.required_field.asUiText())
            }
            return
        } else {
            _uiState.update { prev -> prev.copy(clazzNameError = null) }
        }

        launchWithLoadingIndicator {
            try {
                if (route.guid == null) {
                    val newClazz = clazz.copy()

                    createClassUseCase(newClazz)

                    _navCommandFlow.tryEmit(
                        NavCommand.Navigate(
                            ClazzDetail(guid), popUpTo = route, popUpToInclusive = true
                        )
                    )
                } else {
                    schoolDataSource.classDataSource.store(listOf(clazz))
                    _navCommandFlow.tryEmit(NavCommand.PopUp())
                }
            } catch (e: Throwable) {
                //needs to display snack bar here
                e.printStackTrace()
            }
        }
    }

    private fun generateCode(): String {
        return Random.nextInt(DEFAULT_INVITE_CODE_MAX)
            .toString()
            .padStart(DEFAULT_INVITE_CODE_LEN, '0')
    }
    fun onClearError() {
        _uiState.update { prev -> prev.copy(clazzNameError = null) }
    }

}