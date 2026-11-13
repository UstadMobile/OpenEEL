package org.openeel.shared.viewmodel.manageuser.getstarted

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.update
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.lets_get_started
import org.openeel.shared.navigation.ConnectToExistingLearningSpace
import org.openeel.shared.navigation.CreateNewLearningSpace
import org.openeel.shared.navigation.GetStartedScreen
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel

class GetStartedViewModel(
    savedStateHandle: SavedStateHandle,
) : OpenEelViewModel(savedStateHandle) {

    private val route: GetStartedScreen = savedStateHandle.toRoute()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.lets_get_started.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false,
                showBackButton = route.canGoBack,
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
