package world.respect.shared.viewmodel.assignment.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.school.ext.fullName
import world.respect.datalayer.db.school.ext.isAdminOrTeacher
import world.respect.datalayer.db.school.ext.isStudent
import world.respect.datalayer.school.ext.asXapiAgent
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.opds.model.Publication
import world.respect.lib.xapi.model.AssignmentSummary
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.assignment
import world.respect.shared.generated.resources.assignments
import world.respect.shared.navigation.AssignmentDetail
import world.respect.shared.navigation.AssignmentEdit
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.AssignmentListScreenFilter
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
import kotlin.uuid.ExperimentalUuidApi

data class AssignmentListUiState(
    val assignments: DataLoadState<List<AssignmentSummary>> = DataLoadingState(),
    val learningUnitInfoFlow: (Url) -> Flow<DataLoadState<Publication>> = { emptyFlow() },
    val selectedFilter: AssignmentListScreenFilter = AssignmentListScreenFilter.ALL,
    val isStudent: Boolean = false,
    val personName: String = "",
) {
    private val totalCount: Int
        get() = assignments.dataOrNull()?.size ?: 0

    private val completedCount: Int
        get() = assignments.dataOrNull()?.count { it.isCompleted } ?: 0
    /**
     * Returns the display label for a given filter based on current state counts.
     */
    fun getLabelForFilter(filter: AssignmentListScreenFilter): String {
        return when (filter) {
            AssignmentListScreenFilter.ALL -> filter.displayName
            AssignmentListScreenFilter.COMPLETED -> "${filter.displayName} ($completedCount)"
            AssignmentListScreenFilter.PENDING -> "${filter.displayName} (${totalCount - completedCount})"
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
class AssignmentListViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(AssignmentListUiState())

    val uiState: StateFlow<AssignmentListUiState> = _uiState.asStateFlow()

    private val schoolUrl = accountManager.requireActiveSchoolUrl()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.assignments.asUiText(),
                fabState = FabUiState(
                    text = Res.string.assignment.asUiText(),
                    icon = FabUiState.FabIcon.ADD,
                    onClick = ::onClickAdd
                ),
                showBackButton = false,
            )
        }

        _uiState.update {
            it.copy(learningUnitInfoFlow = ::learningUnitInfoFlowFor)
        }

        viewModelScope.launch {
            accountManager.selectedAccountAndPersonFlow.collect { selectedAcct ->
                val person = selectedAcct?.person
                _uiState.update {
                    it.copy(
                        isStudent = person?.isStudent() == true,
                        personName = person?.fullName() ?: EMPTY_STRING
                    )
                }

                _appUiState.update { prev ->
                    prev.copy(
                        fabState = prev.fabState.copy(
                            visible = person?.isAdminOrTeacher() == true
                        )
                    )
                }

                val selectedStudent = selectedAcct?.person?.takeIf { it.isStudent() }
                schoolDataSource.xapiResource.statements.getAssignmentListAsFlow(
                    dataLoadParams = DataLoadParams(),
                    studentAgent = selectedStudent?.asXapiAgent(schoolUrl)
                ).collectLatest { assignmentSummaries ->
                    _uiState.update {
                        it.copy(assignments = assignmentSummaries)
                    }
                }
            }
        }
    }



    fun onFilterChanged(filter: AssignmentListScreenFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun onClickAssignment(summary: AssignmentSummary) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                AssignmentDetail(
                    assignmentActivityId = summary.activityId
                )
            )
        )
    }

    fun onClickAdd() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                AssignmentEdit.create(
                    assignmentActivityId = null
                )
            )
        )
    }

    fun learningUnitInfoFlowFor(url: Url): Flow<DataLoadState<Publication>> {
        return schoolDataSource.opdsPublicationDataSource.getByUrlAsFlow(
            url = url, params = DataLoadParams(), null, null
        )
    }

    companion object {
        private const val EMPTY_STRING = ""
    }
}
