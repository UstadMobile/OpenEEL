package org.openeel.shared.viewmodel.report.indictor.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.datalayer.SchoolDataSource
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.xapi.extensions.reportoptions.Indicator
import org.openeel.shared.domain.account.RespectAccountManager
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.indicator
import org.openeel.shared.generated.resources.indicators
import org.openeel.shared.navigation.IndicatorDetail
import org.openeel.shared.navigation.IndictorEdit
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.RespectViewModel
import org.openeel.shared.viewmodel.app.appstate.FabUiState

data class IndicatorListUiState(
    val indicators: List<Indicator> = emptyList(),
    val errorMessage: String? = null
)

class IndicatorListViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    private val _uiState = MutableStateFlow(IndicatorListUiState())
    val uiState = _uiState.asStateFlow()
    override val scope: Scope = accountManager.requireActiveAccountScope()
    private val schoolDataSource: SchoolDataSource by inject()

    init {
        viewModelScope.launch {
            _appUiState.update { prev ->
                prev.copy(
                    navigationVisible = true,
                    title = Res.string.indicators.asUiText(),
                    fabState = FabUiState(
                        text = Res.string.indicator.asUiText(),
                        icon = FabUiState.FabIcon.ADD,
                        onClick = { this@IndicatorListViewModel.onClickAdd() },
                        visible = true
                    )
                )
            }
            viewModelScope.launch {
                try {
                    schoolDataSource.indicatorDataSource.allIndicatorAsFlow()
                        .collect { dataLoadState ->
                            _uiState.update { state ->
                                state.copy(indicators = dataLoadState.dataOrNull() ?: emptyList())
                            }
                        }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            errorMessage = e.message ?: "Failed to load indicators"
                        )
                    }
                }
            }
        }
    }

    fun onClickAdd() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                IndictorEdit(null)
            )
        )
    }

    fun onIndicatorSelected(indicator: Indicator) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                IndicatorDetail(
                    indicator.indicatorId
                )
            )
        )
    }
}