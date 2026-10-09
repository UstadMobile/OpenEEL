package world.respect.shared.viewmodel.statement.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.dataloadstate.ext.map
import world.respect.lib.xapi.ext.objectActivityNameOrNull
import world.respect.lib.xapi.ext.objectActivityOrNull
import world.respect.lib.xapi.ext.sortedByTimestampDescending
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.StatementDetail
import world.respect.shared.navigation.StatementList
import world.respect.shared.util.ext.asLangMapUiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel


data class StatementListUiState(
    val statements: DataLoadState<List<XapiStatement>> = DataLoadingState(),
)

class StatementListViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val _uiState = MutableStateFlow(StatementListUiState())

    val uiState: StateFlow<StatementListUiState> = _uiState.asStateFlow()

    private val schoolDataSource: SchoolDataSource by inject()

    private val route: StatementList = savedStateHandle.toRoute()


    init {
        _appUiState.update {
            it.copy(
                showBackButton = true,
            )
        }

        viewModelScope.launch {
            schoolDataSource.xapiResource.statements.getAsFlow(
                listParams = XapiStatementsResource.GetStatementParams(
                    activity = route.activityId,
                    relatedActivities = true,
                    agent = route.xapiActor,
                ),
                dataLoadParams = DataLoadParams()
            ).collectLatest { loadState ->

                val statements = loadState.dataOrNull()?.statements ?: emptyList()

                val unitNameMap = statements.find {
                    it.objectActivityOrNull()?.id == route.activityId
                }?.objectActivityNameOrNull()

                val actorName = route.xapiActor.name ?: ""

                val title = unitNameMap?.mapValues { "${it.value}: $actorName" }?.asLangMapUiText()
                    ?: "${route.activityId.substringAfterLast("/")}: $actorName".asUiText()

                _appUiState.update { it.copy(title = title) }

                _uiState.update {
                    it.copy(
                        statements = loadState.map { result ->
                            result.statements.sortedByTimestampDescending()
                        }
                    )
                }
            }
        }
    }

    fun onClickListItem(statement: XapiStatement) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(StatementDetail(statement.id.toString()))
        )
    }
}
