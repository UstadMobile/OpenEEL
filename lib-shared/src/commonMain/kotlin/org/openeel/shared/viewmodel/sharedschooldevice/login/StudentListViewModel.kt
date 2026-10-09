package org.openeel.shared.viewmodel.sharedschooldevice.login

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
import org.openeel.datalayer.SchoolDataSource
import org.openeel.datalayer.school.PersonDataSource
import org.openeel.datalayer.school.model.EnrollmentRoleEnum
import org.openeel.datalayer.school.model.Person
import org.openeel.datalayer.school.model.PersonStatusEnum
import org.openeel.datalayer.school.writequeue.EnqueueRunPullSyncUseCase
import org.openeel.datalayer.shared.paging.EmptyPagingSourceFactory
import org.openeel.datalayer.shared.paging.IPagingSourceFactory
import org.openeel.datalayer.shared.paging.PagingSourceFactoryHolder
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.libutil.util.time.localDateInCurrentTimeZone
import org.openeel.shared.domain.account.RespectAccountManager
import org.openeel.shared.navigation.AssignmentList
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.StudentList
import org.openeel.shared.navigation.WaitingForApproval
import org.openeel.shared.resources.UiText
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.RespectViewModel

data class StudentListUiState(
    val error: UiText? = null,
    val students: IPagingSourceFactory<Int, Person> = EmptyPagingSourceFactory(),
)

class StudentListViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()
    private val route: StudentList = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(StudentListUiState())
    val uiState = _uiState.asStateFlow()

    private val pagingSourceHolder = PagingSourceFactoryHolder {
        val params = PersonDataSource.GetListParams(
            filterByClazzUid = route.guid,
            filterByEnrolmentRole = EnrollmentRoleEnum.STUDENT,
            inClassOnDay = localDateInCurrentTimeZone()
        )
        val result = schoolDataSource.personDataSource.listAsPagingSource(
            loadParams = DataLoadParams(),
            params = params
        )
        result
    }
    private val enqueuePullSyncUseCase: EnqueueRunPullSyncUseCase by inject()


    init {
        _appUiState.update {
            it.copy(
                title = route.className.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false
            )
        }

        _uiState.update { prev ->
            prev.copy(
                students = pagingSourceHolder,
            )
        }
        viewModelScope.launch {
            enqueuePullSyncUseCase()
        }
    }

    fun onClickStudent(person: Person) {
        viewModelScope.launch {
            try {
                accountManager.switchProfile(person.guid)
                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(
                        destination = if (person.status != PersonStatusEnum.PENDING_APPROVAL) {
                            AssignmentList
                        } else {
                            WaitingForApproval()
                        },
                        clearBackStack = true
                    )
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message?.asUiText(),
                    )
                }
            }
        }
    }
}
