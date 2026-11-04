package org.openeel.shared.viewmodel.enrollment.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataLoadingState
import org.openeel.datalayer.SchoolDataSource
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.datalayer.school.EnrollmentDataSource
import org.openeel.datalayer.school.model.Enrollment
import org.openeel.datalayer.school.model.Person
import org.openeel.datalayer.school.model.StatusEnum
import org.openeel.datalayer.shared.paging.EmptyPagingSourceFactory
import org.openeel.datalayer.shared.paging.IPagingSourceFactory
import org.openeel.datalayer.shared.paging.PagingSourceFactoryHolder
import org.openeel.shared.domain.account.RespectAccountManager
import org.openeel.shared.navigation.EnrollmentEdit
import org.openeel.shared.navigation.EnrollmentList
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.RespectViewModel
import kotlin.time.Clock

data class EnrollmentListUiState(
    val enrollments: IPagingSourceFactory<Int, Enrollment> = EmptyPagingSourceFactory(),
    val forPerson: DataLoadState<Person> = DataLoadingState(),
)

class EnrollmentListViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val _uiState = MutableStateFlow(EnrollmentListUiState())

    val uiState = _uiState.asStateFlow()

    private val schoolDataSource: SchoolDataSource by inject()

    private val route: EnrollmentList = savedStateHandle.toRoute()

    private val pagingSourceHolder = PagingSourceFactoryHolder {
        schoolDataSource.enrollmentDataSource.listAsPagingSource(
            loadParams = DataLoadParams(),
            listParams = EnrollmentDataSource.GetListParams(
                classUid = route.filterByClassUid,
                personUid = route.filterByPersonUid,
                role = route.role,
            )
        )
    }

    init {
        _uiState.update {
            it.copy(enrollments = pagingSourceHolder)
        }

        viewModelScope.launch {
            schoolDataSource.classDataSource.findByGuidAsFlow(
                guid = route.filterByClassUid
            ).collect { clazz ->
                _appUiState.update {
                    it.copy(title = clazz.dataOrNull()?.title?.asUiText())
                }
            }
        }

        viewModelScope.launch {
            schoolDataSource.personDataSource.findByGuidAsFlow(
                guid = route.filterByPersonUid
            ).collect { person ->
                _uiState.update { it.copy(forPerson = person) }
            }
        }
    }

    fun onEditEnrollment(enrollment: Enrollment?) {
        if (enrollment != null) {
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    EnrollmentEdit(
                        uid = enrollment.uid,
                        role = route.role.value,//TODO: THIS IS WRONG. SHOULD HAVE UID ONLY
                        route.filterByPersonUid,
                        route.filterByClassUid
                    )
                )
            )
        }
    }

    fun onDeleteEnrollment(enrollment: Enrollment) {
        viewModelScope.launch {
            schoolDataSource.enrollmentDataSource.store(
                listOf(
                    enrollment.copy(
                        status = StatusEnum.TO_BE_DELETED,
                        lastModified = Clock.System.now(),
                    )
                )
            )
        }
    }

}
