package org.openeel.shared.viewmodel.statement.detail

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
import org.openeel.lib.dataloadstate.ext.firstOrNotLoaded
import org.openeel.lib.dataloadstate.ext.map
import org.openeel.lib.xapi.ext.objectActivityNameOrNull
import org.openeel.lib.xapi.ext.objectActivityOrNull
import org.openeel.lib.xapi.model.XapiStatement
import org.openeel.lib.xapi.resources.XapiStatementsResource
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.more_options
import org.openeel.shared.generated.resources.show_raw_xapi
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.RawStatement
import org.openeel.shared.navigation.StatementDetail
import org.openeel.shared.resources.LangMapUiText
import org.openeel.shared.resources.StringUiText
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel
import org.openeel.shared.viewmodel.app.appstate.AppActionButton
import org.openeel.shared.viewmodel.app.appstate.AppStateIcon
import kotlin.uuid.Uuid

data class StatementDetailUiState(
    val statements: DataLoadState<XapiStatement> = DataLoadingState(),
)

class StatementDetailViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: AppAccountManager,
) : OpenEelViewModel(savedStateHandle), KoinScopeComponent {

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
