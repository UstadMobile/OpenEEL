package world.respect.shared.viewmodel.assignment.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.school.ext.isAdminOrTeacher
import world.respect.datalayer.db.school.ext.isStudent
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.opds.model.Publication
import world.respect.lib.xapi.composites.AssignmentAndProgress
import world.respect.lib.xapi.composites.XapiActorAndAssignmentProgress
import world.respect.lib.xapi.ext.isCompleted
import world.respect.lib.xapi.ext.isInProgress
import world.respect.lib.xapi.ext.isNotStarted
import world.respect.lib.xapi.ext.isStarted
import world.respect.lib.xapi.ext.objectActivityNameOrNull
import world.respect.lib.xapi.ext.webPubManifestAsUrlOrNull
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiActor
import world.respect.lib.xapi.model.XapiVerb
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.edit
import world.respect.shared.generated.resources.invalid_link
import world.respect.shared.navigation.AssignmentDetail
import world.respect.shared.navigation.AssignmentEdit
import world.respect.shared.navigation.PublicationDetail
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.StatementList
import world.respect.shared.util.AssignmentStatusFilter
import world.respect.shared.util.ext.asLangMapUiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
import world.respect.shared.viewmodel.app.appstate.Snack
import world.respect.shared.viewmodel.app.appstate.SnackBarDispatcher
import kotlin.collections.emptyList

/**
 *
 */
data class AssignmentDetailUiState(
    val taskInfoFlow: (Url) -> Flow<DataLoadState<Publication>> = {
        flowOf(DataLoadingState())
    },
    val assignmentProgress: DataLoadState<AssignmentAndProgress> = DataLoadingState(),
    val selectedStatusFilter: AssignmentStatusFilter = AssignmentStatusFilter.ALL,
    val isFullscreen: Boolean = false,
    val isStudent: Boolean = false,
    val canEdit: Boolean = false,
) {

    /**
     * All tasks associated with this assignment, extracted from the assignment's xAPI definition.
     */
    val tasks: List<XapiActivity>
        get() = assignmentProgress.dataOrNull()?.assignmentStatement?.context?.contextActivities?.grouping ?: emptyList()


    val assignmentProgressList: List<XapiActorAndAssignmentProgress>
        get() = assignmentProgress.dataOrNull()?.progress ?: emptyList()

    val numTotal: Int
        get() = if(isStudent) {
            assignmentProgressList.firstOrNull()?.progressPerTask?.size ?: 0
        }else {
            assignmentProgressList.size
        }

    val numCompleted: Int by lazy {
        if(isStudent) {
            assignmentProgressList.firstOrNull()?.progressPerTask?.count { it.isCompleted() } ?: 0
        }else {
            assignmentProgressList.count { it.isCompleted() }
        }
    }

    val numInProgress: Int by lazy {
        if(isStudent) {
            assignmentProgressList.firstOrNull()?.progressPerTask?.count {
                (it.progress ?: 0) > 0 && !it.isCompleted()
            } ?: 0
        }else {
            assignmentProgressList.count { it.isStarted && !it.isCompleted() }
        }

    }

    val numNotStarted: Int by lazy {
        if(isStudent) {
            assignmentProgressList.firstOrNull()?.progressPerTask?.count {
                it.isNotStarted()
            } ?: 0
        }else {
            assignmentProgressList.count { !it.isStarted }
        }

    }

    val rowsToDisplay: List<XapiActorAndAssignmentProgress> by lazy {
        /*
         * In student mode we filter the task list according to the selected status filter.
         */
        if(isStudent) {
            assignmentProgressList.firstOrNull()?.let { studentProgress ->
                listOf(
                    XapiActorAndAssignmentProgress(
                        actor = studentProgress.actor,
                        progressPerTask = studentProgress.progressPerTask.filter {
                            when(selectedStatusFilter) {
                                AssignmentStatusFilter.ALL -> true
                                AssignmentStatusFilter.COMPLETED -> it.isCompleted()
                                AssignmentStatusFilter.IN_PROGRESS -> it.isInProgress()
                                AssignmentStatusFilter.NOT_STARTED -> it.isNotStarted()
                            }
                        }
                    )
                )
            } ?: emptyList()
        }else {
            /*
             * In teacher/admin mode we filter the list of students according to the status filter.
             */
            when (selectedStatusFilter) {
                AssignmentStatusFilter.ALL -> assignmentProgressList
                AssignmentStatusFilter.COMPLETED -> assignmentProgressList.filter {
                    it.isCompleted()
                }

                AssignmentStatusFilter.IN_PROGRESS -> assignmentProgressList.filter {
                    it.isStarted && !it.isCompleted()
                }

                AssignmentStatusFilter.NOT_STARTED -> assignmentProgressList.filter {
                    !it.isStarted
                }
            }
        }
    }

    val statusCounts: Map<AssignmentStatusFilter, Int>
        get() = mapOf(
            AssignmentStatusFilter.ALL to numTotal,
            AssignmentStatusFilter.COMPLETED to numCompleted,
            AssignmentStatusFilter.IN_PROGRESS to numInProgress,
            AssignmentStatusFilter.NOT_STARTED to numNotStarted
        )

}
class AssignmentDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val snackBarDispatcher: SnackBarDispatcher,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val route: AssignmentDetail = savedStateHandle.toRoute()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(AssignmentDetailUiState())

    val uiState = _uiState.asStateFlow()

    private val assignmentActivityId: String = route.assignmentActivityId


    // Create a shared flow for assignment progress

    init {
        _appUiState.update {
            it.copy(
                fabState = FabUiState(
                    text = Res.string.edit.asUiText(),
                    icon = FabUiState.FabIcon.EDIT,
                    onClick = ::onClickEdit
                )
            )
        }

        _uiState.update {
            it.copy(
                taskInfoFlow = ::taskInfoFlowFor
            )
        }

        viewModelScope.launch {
            accountManager.selectedAccountAndPersonFlow.filterNotNull().distinctUntilChanged().collectLatest { sessionAndPerson ->
                val isStudent = sessionAndPerson.person.isStudent()
                val canEdit = sessionAndPerson.person.isAdminOrTeacher()

                _uiState.update {
                    it.copy(
                        isStudent = isStudent,
                        canEdit = canEdit
                    )
                }

                val assignmentProgressFlow = schoolDataSource.xapiResource.statements.getAssignmentProgress(
                    activityId = route.assignmentActivityId,
                    filterByAssigneeAgent = if(sessionAndPerson.person.isStudent()) {
                        sessionAndPerson.xapiAgent
                    }else {
                        null
                    }
                ).map { state ->
                    if (state is DataReadyState) {
                        state.copy(
                            data = state.data.copy(
                                progress = state.data.progress.sortedBy { it.actor.name?.lowercase() ?: "" }
                            )
                        )
                    } else state
                }.shareIn(viewModelScope, SharingStarted.Lazily)

                launch {
                    assignmentProgressFlow.collect { assignmentAndProgress ->
                        _appUiState.update { appState ->
                            appState.copy(
                                title = assignmentAndProgress.dataOrNull()?.assignmentStatement
                                    ?.objectActivityNameOrNull()?.asLangMapUiText()
                            )
                        }

                        _uiState.update {
                            it.copy(assignmentProgress = assignmentAndProgress)
                        }
                    }
                }

                launch {
                    //Load the statement for the assigned actor (e.g. group) to ensure the entire definition is loaded
                    assignmentProgressFlow.mapNotNull {
                        it.dataOrNull()?.assignmentStatement?.actor
                    }.distinctUntilChanged().collect { assignedActor ->
                        schoolDataSource.xapiResource.statements.get(
                            listParams = XapiStatementsResource.GetStatementParams(
                                agent = assignedActor,
                                verb = XapiVerb.ID_SAVED,
                            )
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            _uiState.map { Pair(it.isFullscreen, it.canEdit) }
                .distinctUntilChanged()
                .collect { (isFullscreen, canEdit) ->
                    _appUiState.update {
                        it.copy(
                            hideAppBar = isFullscreen,
                            hideBottomNavigation = isFullscreen,
                            fabState = it.fabState.copy(
                                visible = canEdit && !isFullscreen
                            )
                        )
                    }
                }
        }

    }

    fun onClickEdit() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                AssignmentEdit.create(
                    assignmentActivityId = route.assignmentActivityId
                )
            )
        )
    }

    fun taskInfoFlowFor(url: Url): Flow<DataLoadState<Publication>> {
        return schoolDataSource.opdsPublicationDataSource.getByUrlAsFlow(
            url = url, params = DataLoadParams(), null, null
        )
    }

    fun onClickTask(activity: XapiActivity) {
        val manifestUrl = activity.definition?.webPubManifestAsUrlOrNull()

        if(manifestUrl == null) {
            snackBarDispatcher.showSnackBar(Snack(Res.string.invalid_link.asUiText()))
            return
        }

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                PublicationDetail.create(
                    learningUnitManifestUrl = manifestUrl,
                    assignmentActivityId = assignmentActivityId,
                    expectedIdentifier = activity.id,
                )
            )
        )
    }

    fun onStatusFilterChanged(filter: AssignmentStatusFilter) {
        _uiState.update {
            it.copy(selectedStatusFilter = filter)
        }
    }

    fun onToggleFullscreen() {
        _uiState.update { it.copy(isFullscreen = !it.isFullscreen) }
    }
    fun onClickScoreCell(activityId: String, xapiActor: XapiActor) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                StatementList.create(
                    activityId = activityId,
                    xapiActor = xapiActor
                )
            )
        )
    }
}
