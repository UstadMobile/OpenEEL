package org.openeel.app.viewmodel

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel
import org.openeel.shared.viewmodel.app.appstate.AppUiState
import org.openeel.app.effects.AppUiStateEffect
import org.openeel.navigation.NavCommandEffect
import org.openeel.shared.navigation.OpenEelComposeNavController
import org.openeel.shared.viewmodel.OpenEelViewModel

@Composable
inline fun <reified T : OpenEelViewModel> openEelViewModel(
    noinline onSetAppUiState: (AppUiState) -> Unit,
    navController: OpenEelComposeNavController,
): T {

    val viewModel: T = koinViewModel()

    AppUiStateEffect(
        appUiStateFlow = viewModel.appUiState,
        onSetAppUiState = onSetAppUiState,
    )

    NavCommandEffect(
        navHostController = navController,
        navCommandFlow = viewModel.navCommandFlow
    )

    return viewModel
}

