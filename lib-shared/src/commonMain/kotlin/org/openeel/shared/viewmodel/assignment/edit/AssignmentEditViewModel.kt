package org.openeel.shared.viewmodel.assignment.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.datalayer.SchoolDataSource
import org.openeel.datalayer.school.ClassDataSource
import org.openeel.datalayer.school.model.Clazz
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataLoadingState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.dataloadstate.ext.firstOrNotLoaded
import org.openeel.lib.dataloadstate.ext.isReadyAndSettled
import org.openeel.lib.dataloadstate.ext.map
import org.openeel.lib.opds.model.Publication
import org.openeel.lib.xapi.ext.addActivitiesToContextActivitiesGrouping
import org.openeel.lib.xapi.ext.mostRecentByTimestampOrNull
import org.openeel.lib.xapi.ext.objectActivityNameOrNull
import org.openeel.lib.xapi.ext.objectActivityOrNull
import org.openeel.lib.xapi.ext.removeActivityFromContextActivitiesGrouping
import org.openeel.lib.xapi.model.XapiActivity
import org.openeel.lib.xapi.model.XapiStatement
import org.openeel.lib.xapi.model.XapiVerb
import org.openeel.lib.xapi.resources.XapiStatementsResource.GetStatementParams
import org.openeel.libutil.ext.appendEndpointSegments
import org.openeel.libutil.ext.isNullOrAllBlank
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.domain.opds.getxapiactivityid.GetXapiActivityForPublicationUseCase
import org.openeel.shared.domain.xapi.createBlankAssignmentStatement
import org.openeel.shared.ext.studentsXapiGroup
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.add_assignment
import org.openeel.shared.generated.resources.could_not_load_unit_to_assign
import org.openeel.shared.generated.resources.edit_assignment
import org.openeel.shared.generated.resources.required_field
import org.openeel.shared.generated.resources.save
import org.openeel.shared.navigation.AssignmentDetail
import org.openeel.shared.navigation.AssignmentEdit
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.NavResultReturner
import org.openeel.shared.navigation.OpenEelAppLauncher
import org.openeel.shared.navigation.RouteResultDest
import org.openeel.shared.resources.UiText
import org.openeel.shared.util.LaunchDebouncer
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel
import org.openeel.shared.viewmodel.app.appstate.ActionBarButtonUiState
import org.openeel.shared.viewmodel.app.appstate.Snack
import org.openeel.shared.viewmodel.app.appstate.SnackBarDispatcher
import org.openeel.shared.viewmodel.catalog.PublicationsSelection
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class AssignmentEditUiState(
    val statementData: DataLoadState<XapiStatement> = DataLoadingState(),
    val assignee: String = "",
    val nameError: UiText? = null,
    val classOptions: List<Clazz> = emptyList(),
    val classError: UiText? = null,
    val learningUnitInfoFlow: (Url) -> Flow<DataLoadState<Publication>> = { flowOf(DataLoadingState()) },
) {
    val fieldsEnabled: Boolean
        get() = statementData.isReadyAndSettled()

    val hasErrors: Boolean
        get() = nameError != null || classError != null
}

@OptIn(ExperimentalUuidApi::class)
class AssignmentEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: AppAccountManager,
    private val json: Json,
    private val resultReturner: NavResultReturner,
    private val snackBarDispatcher: SnackBarDispatcher,
) : OpenEelViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val route: AssignmentEdit = savedStateHandle.toRoute()

    private val schoolDataSource: SchoolDataSource by inject()

    private val getXapiActivityForPublicationUseCase: GetXapiActivityForPublicationUseCase by inject()

    private val _uiState = MutableStateFlow(
        AssignmentEditUiState(
            learningUnitInfoFlow = ::learningUnitInfoFlowFor
        )
    )

    val uiState = _uiState.asStateFlow()

    private val debouncer = LaunchDebouncer(viewModelScope)

    private val schoolUrl = accountManager.requireActiveSchoolUrl()

    private val assignmentActivityId = route.assignmentActivityId ?: run {
        schoolUrl.appendEndpointSegments(ACTIVITY_ID_PATH, Uuid.random().toString()).toString()
    }

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = if (route.assignmentActivityId == null) {
                    Res.string.add_assignment.asUiText()
                } else {
                    Res.string.edit_assignment.asUiText()
                },
                userAccountIconVisible = false,
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = Res.string.save.asUiText(),
                    onClick = ::onClickSave,
                ),
                hideBottomNavigation = true,
            )
        }

        launchWithLoadingIndicator(
            onShowError = { snackBarDispatcher.showSnackBar(Snack(it)) }
        ) {
            val classes = schoolDataSource.classDataSource.list(
                DataLoadParams(),
                ClassDataSource.GetListParams()
            ).dataOrNull() ?: emptyList()

            _uiState.update {
                it.copy(
                    classOptions = classes,
                )
            }

            if (route.assignmentActivityId != null) {
                loadEntity(
                    json = json,
                    serializer = XapiStatement.serializer(),
                    loadFn = { params ->
                        schoolDataSource.xapiResource.statements.get(
                            listParams = GetStatementParams(
                                activity = assignmentActivityId,
                                verb = XapiVerb.ID_ASSIGN,
                            ),
                            dataLoadParams = params
                        ).map { result ->
                            result.statements.mostRecentByTimestampOrNull()?.let {
                                listOf(it)
                            } ?: emptyList()
                        }.firstOrNotLoaded()
                    },
                    uiUpdateFn = { entity ->
                        _uiState.update { prev ->
                            prev.copy(
                                statementData = entity,
                                assignee = entity.dataOrNull()?.actor?.name.orEmpty()
                            )
                        }
                    }
                )
            } else {
                val instructor = accountManager.selectedAccountAndPersonFlow.first()?.xapiAgent
                    ?: return@launchWithLoadingIndicator
                val baseStmt = createBlankAssignmentStatement(
                    assignmentActivityId = assignmentActivityId,
                    instructor = instructor
                )

                val initialStmt = route.learningUnitSelected?.let {
                    try {
                        baseStmt.addActivitiesToContextActivitiesGrouping(
                            getXapiActivityForPublicationUseCase(it.selectedPublications)
                        )
                    }catch(e: Throwable) {
                        Napier.w("Could not load activity unit for assignment init", e)
                        snackBarDispatcher.showSnackBar(Snack(Res.string.could_not_load_unit_to_assign.asUiText()))
                        null
                    }
                } ?: baseStmt

                _uiState.update { prev ->
                    prev.copy(
                        statementData = DataReadyState(initialStmt)
                    )
                }
            }

            viewModelScope.launch {
                resultReturner.filteredResultFlowForKey(KEY_LEARNING_UNIT).collect { result ->
                    val learningUnit = result.result as? PublicationsSelection ?: return@collect
                    val activities = getXapiActivityForPublicationUseCase(learningUnit.selectedPublications)

                    _uiState.update { prev ->
                        val preStatementData = prev.statementData.dataOrNull() ?: return@update prev

                        prev.copy(
                            statementData = DataReadyState(
                                data = preStatementData.addActivitiesToContextActivitiesGrouping(
                                    activities
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    fun learningUnitInfoFlowFor(url: Url): Flow<DataLoadState<Publication>> {
        return schoolDataSource.opdsPublicationDataSource.getByUrlAsFlow(
            url = url, params = DataLoadParams(), null, null
        )
    }

    fun onAssigneeClassSelected(clazz: Clazz) {
        val statement = _uiState.value.statementData.dataOrNull() ?: return
        _uiState.update {
            it.copy(
                statementData = DataReadyState(
                    statement.copy(actor = clazz.studentsXapiGroup(schoolUrl))
                ),
                assignee = clazz.title,
                classError = null,
            )
        }
    }

    fun onEntityChanged(statement: XapiStatement) {
        _uiState.update { prev ->
            prev.copy(
                statementData = DataReadyState(statement),
                nameError = prev.nameError?.takeIf {
                    prev.statementData.dataOrNull()?.objectActivityNameOrNull() == statement.objectActivityNameOrNull()
                },
            )
        }

        debouncer.launch(DEFAULT_SAVED_STATE_KEY) {
            savedStateHandle[DEFAULT_SAVED_STATE_KEY] =
                json.encodeToString(XapiStatement.serializer(), statement)
        }
    }

    fun onAssigneeTextChanged(text: String) {
        _uiState.update {
            it.copy(assignee = text, classError = null)
        }
    }


    fun onClickAddLearningUnit() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                OpenEelAppLauncher.create(
                    resultDest = RouteResultDest(
                        resultPopUpTo = route,
                        resultKey = KEY_LEARNING_UNIT,
                    )
                )
            )
        )
    }

    fun onClickRemoveLearningUnit(
        activity: XapiActivity
    ) {
        val assignment = uiState.value.statementData.dataOrNull() ?: return

        _uiState.update { prev ->
            prev.copy(
                statementData = DataReadyState(
                    data = assignment.removeActivityFromContextActivitiesGrouping(
                        idToRemove = activity.id
                    )
                )
            )
        }
    }

    fun onClickSave() {
        val stateToSave = _uiState.updateAndGet { prev ->
            val statement = prev.statementData.dataOrNull()

            prev.copy(
                nameError = Res.string.required_field.asUiText().takeIf {
                    statement?.objectActivityOrNull()?.definition?.name.isNullOrAllBlank()
                },
                classError = Res.string.required_field.asUiText().takeIf {
                    statement?.actor?.name.isNullOrEmpty()
                }
            )
        }

        if (stateToSave.hasErrors)
            return

        val assignment = uiState.value.statementData.dataOrNull() ?: return

        launchWithLoadingIndicator {
            schoolDataSource.xapiResource.statements.post(
                listOf(
                    assignment.copy(
                        id = Uuid.random(),
                        timestamp = Clock.System.now(),
                    )
                )
            )

            if (route.assignmentActivityId == null) {
                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(
                        destination = AssignmentDetail(
                            assignmentActivityId = assignmentActivityId
                        ),
                        popUpTo = route,
                        popUpToInclusive = true,
                    )
                )
            } else {
                _navCommandFlow.tryEmit(NavCommand.PopUp())
            }
        }
    }
    companion object {

        const val KEY_LEARNING_UNIT = "result_learning_unit_single"

        const val ACTIVITY_ID_PATH = "xapi/activities/assignment"

        /**
         * If too many units are passed
         */
        const val MAX_UNITS_TO_ADD = 20

    }
}



