package org.openeel.shared.util.ext

import org.openeel.shared.viewmodel.app.appstate.AppUiState
import org.openeel.shared.viewmodel.app.appstate.LoadingUiState

val AppUiState.isLoading: Boolean
    get() = loadingState.loadingState == LoadingUiState.State.INDETERMINATE

