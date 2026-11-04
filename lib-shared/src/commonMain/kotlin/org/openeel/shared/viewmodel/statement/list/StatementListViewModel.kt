package org.openeel.shared.viewmodel.statement.list

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
import org.openeel.datalayer.SchoolDataSource
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataLoadingState
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.dataloadstate.ext.map
import org.openeel.lib.xapi.ext.objectActivityNameOrNull
import org.openeel.lib.xapi.ext.objectActivityOrNull
import org.openeel.lib.xapi.ext.sortedByTimestampDescending
import org.openeel.lib.xapi.model.XapiStatement
import org.openeel.lib.xapi.resources.XapiStatementsResource
import org.openeel.shared.domain.account.RespectAccountManager
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.StatementDetail
import org.openeel.shared.navigation.StatementList
import org.openeel.shared.util.ext.asLangMapUiText
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.RespectViewModel


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
