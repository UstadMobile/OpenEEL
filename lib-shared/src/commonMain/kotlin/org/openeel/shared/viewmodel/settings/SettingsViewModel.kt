package world.respect.shared.viewmodel.settings

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.settings
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.SchoolSettings
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class SettingsUiState(
    val loading: Boolean = false,
)

class SettingsViewModel(
    savedStateHandle: SavedStateHandle,
    private val json: Json,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: Flow<SettingsUiState> = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.settings.asUiText(),
                navigationVisible = true,
                hideAppBar = false,
                userAccountIconVisible = true,
                hideBottomNavigation = true,
            )
        }
    }

    fun onNavigateToLanguage() {
        // TODO
    }

    fun onClickSchool() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(SchoolSettings)
        )
    }
}