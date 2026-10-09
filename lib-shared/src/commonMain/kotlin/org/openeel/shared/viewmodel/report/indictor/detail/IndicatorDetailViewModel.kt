package world.respect.shared.viewmodel.report.indictor.detail

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
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.xapi.extensions.reportoptions.Indicator
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.edit
import world.respect.shared.generated.resources.indicator_detail
import world.respect.shared.navigation.IndicatorDetail
import world.respect.shared.navigation.IndictorEdit
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState

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