package world.respect.shared.util.ext

import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.shared.viewmodel.app.appstate.LoadingUiState

val AppUiState.isLoading: Boolean
    get() = loadingState.loadingState == LoadingUiState.State.INDETERMINATE

