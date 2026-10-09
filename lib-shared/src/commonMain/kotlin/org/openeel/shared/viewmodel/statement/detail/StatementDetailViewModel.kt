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
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.dataloadstate.ext.firstOrNotLoaded
import world.respect.lib.dataloadstate.ext.map
import world.respect.lib.xapi.ext.objectActivityNameOrNull
import world.respect.lib.xapi.ext.objectActivityOrNull
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.more_options
import world.respect.shared.generated.resources.show_raw_xapi
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.RawStatement
import world.respect.shared.navigation.StatementDetail
import world.respect.shared.resources.LangMapUiText
import world.respect.shared.resources.StringUiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.AppActionButton
import world.respect.shared.viewmodel.app.appstate.AppStateIcon
import kotlin.uuid.Uuid

data class StatementDetailUiState(
    val statements: DataLoadState<XapiStatement> = DataLoadingState(),
)

class StatementDetailViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val _uiState = MutableStateFlow(StatementDetailUiState())

    val uiState: StateFlow<StatementDetailUiState> = _uiState.asStateFlow()

    private val schoolDataSource: SchoolDataSource by inject()

    private val route: StatementDetail = savedStateHandle.toRoute()

    init {
        _appUiState.update {
            it.copy(
                showBackButton = true,
                userAccountIconVisible = false,
                actions = listOf(
                    AppActionButton(
                        icon = AppStateIcon.MORE_VERT,
                        contentDescription = Res.string.more_options.asUiText(),
                        text = Res.string.show_raw_xapi.asUiText(),
                        onClick = { showRawStatement(statementId = route.statementId) },
                        id = "more_options_xapi",
                        display = AppActionButton.Companion.ActionButtonDisplay.OVERFLOW_MENU
                    )
                )

            )
        }

        viewModelScope.launch {
            schoolDataSource.xapiResource.statements.getAsFlow(
                listParams = XapiStatementsResource.GetStatementParams(
                    statementId = Uuid.parse(route.statementId),
                ),
                dataLoadParams = DataLoadParams()
            ).collectLatest { loadState ->
                val statements = loadState.dataOrNull()?.statements ?: emptyList()
                val statement = statements.firstOrNull()

                val unitName = statement?.objectActivityNameOrNull()?.let { LangMapUiText(it) }
                    ?: statement?.objectActivityOrNull()?.id?.substringAfterLast("/")?.let { StringUiText(it) }

                _appUiState.update { it.copy(title = unitName) }

                _uiState.update { state ->
                    state.copy(
                        statements = loadState.map { it.statements }.firstOrNotLoaded()
                    )
                }
            }
        }
    }

    fun showRawStatement(statementId: String) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(RawStatement(statementId))
        )
    }
}
