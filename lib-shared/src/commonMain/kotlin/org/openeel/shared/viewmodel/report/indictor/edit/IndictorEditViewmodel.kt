package world.respect.shared.viewmodel.report.indictor.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.extensions.reportoptions.Indicator
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.school.SchoolPrimaryKeyGenerator
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.done
import world.respect.shared.generated.resources.indicator
import world.respect.shared.navigation.IndicatorDetail
import world.respect.shared.navigation.IndictorEdit
import world.respect.shared.navigation.NavCommand
import world.respect.shared.resources.UiText
import world.respect.shared.util.LaunchDebouncer
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState

data class IndicatorEditUiState(
    val indicatorData: DataLoadState<Indicator> = DataLoadingState(),
    val nameError: UiText? = null,
    val descriptionError: UiText? = null,
    val sqlError: UiText? = null,
    val errorMessage: String? = null
)

class IndicatorEditViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
    private val json: Json
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()
    private val schoolDataSource: SchoolDataSource by inject()
    private val _uiState = MutableStateFlow(IndicatorEditUiState())
    val uiState = _uiState.asStateFlow()
    private val debouncer = LaunchDebouncer(viewModelScope)
    private val route: IndictorEdit = savedStateHandle.toRoute()
    private val schoolPrimaryKeyGenerator: SchoolPrimaryKeyGenerator by inject()
    private val guid = route.indicatorId ?: schoolPrimaryKeyGenerator.primaryKeyGenerator.nextId(
        Indicator.TABLE_ID
    ).toString()

    init {
        _appUiState.update { prev ->
            prev.copy(
                navigationVisible = true,
                title = Res.string.indicator.asUiText(),
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = Res.string.done.asUiText(),
                    onClick = this@IndicatorEditViewModel::onSaveIndicator
                ),
                userAccountIconVisible = false,
            )
        }
        viewModelScope.launch {
            if (route.indicatorId != null) {
                loadEntity(
                    json = json,
                    serializer = Indicator.serializer(),
                    loadFn = { params ->
                        schoolDataSource.indicatorDataSource.getIndicatorAsync(
                            params,
                            route.indicatorId
                        )
                    },
                    uiUpdateFn = { indicator ->
                        _uiState.update { prev ->
                            prev.copy(
                                indicatorData = indicator
                            )
                        }
                    }
                )
            } else {
                _uiState.update { prev ->
                    prev.copy(
                        indicatorData = DataReadyState(Indicator(indicatorId = guid))
                    )
                }
            }
        }
    }

    fun onEntityChanged(indicator: Indicator) {
        val indicatorToCommit = _uiState.updateAndGet { prev ->
            prev.copy(indicatorData = DataReadyState(indicator))
        }.indicatorData.dataOrNull() ?: return

        debouncer.launch(DEFAULT_SAVED_STATE_KEY) {
            savedStateHandle[DEFAULT_SAVED_STATE_KEY] = json.encodeToString(indicatorToCommit)
        }
    }

    private fun onSaveIndicator() {
        val indicator = _uiState.value.indicatorData.dataOrNull() ?: return
        viewModelScope.launch {
            try {
                schoolDataSource.indicatorDataSource.putIndicator(indicator)

                if (route.indicatorId == null) {
                    _navCommandFlow.tryEmit(
                        NavCommand.Navigate(
                            IndicatorDetail(guid), popUpTo = route, popUpToInclusive = true
                        )
                    )
                } else {
                    _navCommandFlow.tryEmit(NavCommand.PopUp())
                }
            } catch (e: Throwable) {
                _uiState.update {
                    it.copy(
                        errorMessage = e.message ?: "Error updating indicator"
                    )
                }
            }
        }
    }
}