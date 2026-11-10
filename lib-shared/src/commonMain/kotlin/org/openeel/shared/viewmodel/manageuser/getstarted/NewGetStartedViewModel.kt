package org.openeel.shared.viewmodel.manageuser.getstarted

import androidx.lifecycle.SavedStateHandle
import org.openeel.shared.viewmodel.OpenEelViewModel
import kotlinx.coroutines.flow.update
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.lets_get_started
import org.openeel.shared.navigation.ConnectToExistingLearningSpace
import org.openeel.shared.navigation.CreateNewLearningSpace
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.util.ext.asUiText


class NewGetStartedViewModel(
    savedStateHandle: SavedStateHandle,
) : OpenEelViewModel(savedStateHandle) {
    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.lets_get_started.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false,
            )
        }
    }
    fun onClickConnectToExistingLearningSpace() {
        _navCommandFlow.tryEmit(NavCommand.Navigate(ConnectToExistingLearningSpace))
    }

    fun onClickCreateNewLearningSpace() {
        _navCommandFlow.tryEmit(NavCommand.Navigate(CreateNewLearningSpace))
    }
}