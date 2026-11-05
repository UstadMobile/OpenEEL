package org.openeel.shared.viewmodel.manageuser.learningspace

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.openeel.shared.viewmodel.OpenEelViewModel

data class CreateLearningSpaceUiState(
    val link: String = "",
)

class CreateLearningSpaceViewModel(
    savedStateHandle: SavedStateHandle,
) : OpenEelViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(CreateLearningSpaceUiState())
    val uiState = _uiState.asStateFlow()

}