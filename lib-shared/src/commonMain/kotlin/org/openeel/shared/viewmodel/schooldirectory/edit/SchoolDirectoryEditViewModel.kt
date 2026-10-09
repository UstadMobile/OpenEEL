package world.respect.shared.viewmodel.schooldirectory.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.datalayer.RespectAppDataSource
import world.respect.lib.dataloadstate.ext.isReadyAndSettled
import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_directory
import world.respect.shared.generated.resources.error_link_message
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.SchoolDirectoryList
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

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
