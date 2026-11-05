package org.openeel.shared.viewmodel.manageuser.getstarted

import androidx.lifecycle.SavedStateHandle
import org.openeel.shared.viewmodel.OpenEelViewModel
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.update
import org.openeel.shared.domain.navigation.onappstart.NavigateOnAppStartUseCase
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.lets_get_started
import org.openeel.shared.navigation.ConnectLearningSpace
import org.openeel.shared.navigation.CreateLearningSpace
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.util.ext.asUiText


class NewGetStartedViewModel(
    savedStateHandle: SavedStateHandle,
    private val settings: Settings,
    private val navigateOnAppStartUseCase: NavigateOnAppStartUseCase,
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
    fun onClickConnectLearningSpace() {
        _navCommandFlow.tryEmit(NavCommand.Navigate(ConnectLearningSpace))

    }

    fun onClickCreateLearningSpace(){
        _navCommandFlow.tryEmit(NavCommand.Navigate(CreateLearningSpace))

    }
}