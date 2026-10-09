package world.respect.shared.viewmodel.sharedschooldevice.login

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
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonStatusEnum
import world.respect.datalayer.school.writequeue.EnqueueRunPullSyncUseCase
import world.respect.datalayer.shared.paging.EmptyPagingSourceFactory
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.PagingSourceFactoryHolder
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.libutil.util.time.localDateInCurrentTimeZone
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.navigation.AssignmentList
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.StudentList
import world.respect.shared.navigation.WaitingForApproval
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

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
