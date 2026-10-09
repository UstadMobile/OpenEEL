package org.openeel.shared.viewmodel.schooldirectory.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataLoadingState
import org.openeel.datalayer.RespectAppDataSource
import org.openeel.lib.dataloadstate.ext.isReadyAndSettled
import org.openeel.datalayer.respect.model.RespectSchoolDirectory
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.add_directory
import org.openeel.shared.generated.resources.error_link_message
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.SchoolDirectoryList
import org.openeel.shared.resources.UiText
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.RespectViewModel

data class SchoolDirectoryEditUIState(
    val linkUrl: String = "",
    val errorMessage: UiText? = null,
    val schoolDirectory: DataLoadState<RespectSchoolDirectory> = DataLoadingState(),
) {
    val fieldsEnabled: Boolean
        get() = schoolDirectory.isReadyAndSettled()
}

class SchoolDirectoryEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val respectAppDataSource: RespectAppDataSource
) : RespectViewModel(savedStateHandle) {


    private val _uiState = MutableStateFlow(SchoolDirectoryEditUIState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.add_directory.asUiText(),
                hideBottomNavigation = true,
            )
        }
    }

    fun onLinkChanged(link: String) {
        _uiState.update {
            it.copy(
                linkUrl = link,
                errorMessage = null,
            )
        }
    }


    fun onClickAdd() {
        viewModelScope.launch {
            val link = uiState.value.linkUrl.trim()

            try {
                val schoolBaseUrl = Url(link)

                val directory = RespectSchoolDirectory(
                    invitePrefix = "",
                    baseUrl = schoolBaseUrl,
                )

                respectAppDataSource.schoolDirectoryDataSource.insertOrIgnore(directory)

                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(
                        destination = SchoolDirectoryList.create(),
                        popUpTo = SchoolDirectoryList.create(),
                        popUpToInclusive = true
                    )
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = Res.string.error_link_message.asUiText())
                }
            }
        }
    }
}
