package org.openeel.shared.viewmodel.manageuser.learningspace

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.select_where_to_store_data
import org.openeel.shared.navigation.EnterLink
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel

data class CreateLearningSpaceUiState(
    val link: String = "",
)

class CreateLearningSpaceViewModel(
    savedStateHandle: SavedStateHandle,
) : OpenEelViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(CreateLearningSpaceUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _appUiState.update { prev->
                prev.copy(
                    title =Res.string.select_where_to_store_data.asUiText(),
                    hideBottomNavigation = true,
                    userAccountIconVisible = false,
                )
            }

            }
        }

    fun onClickAddLink() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                EnterLink
            )
        )
    }
}