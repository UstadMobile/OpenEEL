package org.openeel.shared.viewmodel.settings

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.settings
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.SchoolSettings
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.RespectViewModel

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