package org.openeel.app.view.manageuser.learningspace

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import org.openeel.shared.viewmodel.manageuser.learningspace.CreateLearningSpaceUiState
import org.openeel.shared.viewmodel.manageuser.learningspace.CreateLearningSpaceViewModel
import androidx.compose.runtime.getValue

@Composable
fun CreateLearningSpaceScreen(
    viewModel: CreateLearningSpaceViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    CreateLearningSpaceScreen(
        uiState = uiState,
    )
}

@Composable
fun CreateLearningSpaceScreen(
    uiState: CreateLearningSpaceUiState,
) {

}
