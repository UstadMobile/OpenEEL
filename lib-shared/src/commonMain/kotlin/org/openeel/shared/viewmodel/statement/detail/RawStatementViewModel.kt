package world.respect.shared.viewmodel.statement.detail

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
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.ext.firstOrNotLoaded
import world.respect.lib.dataloadstate.ext.map
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.raw_statement
import world.respect.shared.navigation.RawStatement
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class RawStatementUiState(
    val statement: DataLoadState<XapiStatement> = DataLoadingState(),
)


class RawStatementViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope = accountManager.requireActiveAccountScope()

    private val _uiState = MutableStateFlow(RawStatementUiState())

    val uiState: StateFlow<RawStatementUiState> = _uiState.asStateFlow()

    private val schoolDataSource: SchoolDataSource by inject()

    private val route: RawStatement = savedStateHandle.toRoute()


    init {
        _appUiState.update {
            it.copy(
                showBackButton = true,
                userAccountIconVisible = false,
                title = Res.string.raw_statement.asUiText(),
            )
        }

        viewModelScope.launch {
            schoolDataSource.xapiResource.statements.getAsFlow(
                listParams = XapiStatementsResource.GetStatementParams(
                    statementId = route.statementId,
                ),
                dataLoadParams = DataLoadParams()
            ).collectLatest { loadState ->
                _uiState.update { state ->
                    state.copy(
                        statement = loadState.map { it.statements }.firstOrNotLoaded()
                    )
                }
            }
        }
    }
}