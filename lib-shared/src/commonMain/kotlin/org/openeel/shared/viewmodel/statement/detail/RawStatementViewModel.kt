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
import org.openeel.datalayer.SchoolDataSource
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataLoadingState
import org.openeel.lib.dataloadstate.ext.firstOrNotLoaded
import org.openeel.lib.dataloadstate.ext.map
import org.openeel.lib.xapi.model.XapiStatement
import org.openeel.lib.xapi.resources.XapiStatementsResource
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.raw_statement
import org.openeel.shared.navigation.RawStatement
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel

data class RawStatementUiState(
    val statement: DataLoadState<XapiStatement> = DataLoadingState(),
)


class RawStatementViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: AppAccountManager,
) : OpenEelViewModel(savedStateHandle), KoinScopeComponent {

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