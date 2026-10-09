package org.openeel.shared.viewmodel.report.indictor.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataLoadingState
import org.openeel.datalayer.SchoolDataSource
import org.openeel.lib.xapi.extensions.reportoptions.Indicator
import org.openeel.shared.domain.account.RespectAccountManager
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.edit
import org.openeel.shared.generated.resources.indicator_detail
import org.openeel.shared.navigation.IndicatorDetail
import org.openeel.shared.navigation.IndictorEdit
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.RespectViewModel
import org.openeel.shared.viewmodel.app.appstate.FabUiState

data class IndicatorDetailUiState(
    val indicator: DataLoadState<Indicator> = DataLoadingState(),
    val errorMessage: String? = null
)

class IndicatorDetailViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()
    private val schoolDataSource: SchoolDataSource by inject()
    private val _uiState = MutableStateFlow(IndicatorDetailUiState())
    val uiState = _uiState.asStateFlow()
    private val route: IndicatorDetail = savedStateHandle.toRoute()

    init {
        viewModelScope.launch {
            _appUiState.update { prev ->
                prev.copy(
                    navigationVisible = true,
                    title = Res.string.indicator_detail.asUiText(),
                    fabState = FabUiState(
                        text = Res.string.edit.asUiText(),
                        icon = FabUiState.FabIcon.EDIT,
                        onClick = {
                            _navCommandFlow.tryEmit(
                                NavCommand.Navigate(
                                    IndictorEdit(route.indicatorUid)
                                )
                            )
                        },
                        visible = true
                    )
                )
            }

            viewModelScope.launch {
                try {
                    schoolDataSource.indicatorDataSource.getIndicatorAsFlow(
                        route.indicatorUid
                    ).collect { indicator ->
                        _uiState.update {
                            it.copy(
                                indicator = indicator,
                                errorMessage = null
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            errorMessage = e.message ?: "Failed to load indicator"
                        )
                    }
                }
            }
        }
    }
}