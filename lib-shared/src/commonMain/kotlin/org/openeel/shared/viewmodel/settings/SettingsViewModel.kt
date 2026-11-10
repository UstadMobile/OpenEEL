package org.openeel.shared.viewmodel.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.openeel.datalayer.db.school.ext.isAdminOrTeacher
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.settings
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel
import org.openeel.shared.navigation.SchoolSettings
import org.openeel.shared.navigation.NavCommand

data class SettingsUiState(
    val showSchoolSettings: Boolean = false,
)

class SettingsViewModel(
    savedStateHandle: SavedStateHandle,
    private val appAccountManager: AppAccountManager,
) : OpenEelViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(SettingsUiState())

    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.settings.asUiText(),
                userAccountIconVisible = true,
                hideBottomNavigation = true,
            )
        }

        viewModelScope.launch {
            appAccountManager.selectedAccountAndPersonFlow.collect { selectedAccountAndPerson ->
                _uiState.update {
                    it.copy(
                        showSchoolSettings = selectedAccountAndPerson?.person?.isAdminOrTeacher() == true
                    )
                }
            }
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