package org.openeel.shared.viewmodel.manageuser.learningspace

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.openeel.datalayer.SchoolDirectoryDataSource
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.libutil.ext.normalizeForEndpoint
import org.openeel.shared.domain.devmode.GetDevModeEnabledUseCase
import org.openeel.shared.domain.urltonavcommand.ResolveUrlToNavCommandUseCase
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.connect_existing_learning_space
import org.openeel.shared.generated.resources.invalid_code
import org.openeel.shared.generated.resources.invalid_url
import org.openeel.shared.navigation.LoginScreen
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.ScanQRCode
import org.openeel.shared.navigation.SchoolDirectoryList
import org.openeel.shared.resources.StringResourceUiText
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel


data class ConnectToExistingLearningSpaceUiState(
    val link: String = "",
    val errorMessage: StringResourceUiText? = null,
    val manageDirectoriesVisible: Boolean = false,
)

class ConnectLearningSpaceViewModel(
    savedStateHandle: SavedStateHandle,
    private val schoolDirectoryDataSource: SchoolDirectoryDataSource,
    private val getDevModeEnabledUseCase: GetDevModeEnabledUseCase,
    private val resolveUrlToNavCommandUseCase: ResolveUrlToNavCommandUseCase,
) : OpenEelViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(ConnectToExistingLearningSpaceUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _appUiState.update { prev ->
                prev.copy(
                    title = Res.string.connect_existing_learning_space.asUiText(),
                    hideBottomNavigation = true,
                    userAccountIconVisible = false,
                )
            }

            getDevModeEnabledUseCase().also { devModeEnabled ->
                _uiState.update { it.copy(manageDirectoriesVisible = devModeEnabled) }
            }
        }
    }

    fun onLinkChanged(link: String) {
        _uiState.update {
            it.copy(
                link = link,
                errorMessage = null,
            )
        }
    }

    fun onClickNext() {
        if (uiState.value.link.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = StringResourceUiText(Res.string.invalid_code))
            }
            return
        }

        launchWithLoadingIndicator {
            try {
                val url = Url(uiState.value.link)
                _navCommandFlow.tryEmit(
                    resolveUrlToNavCommandUseCase(url) ?: url.normalizeForEndpoint().let { schoolUrl ->
                        requireNotNull(
                            schoolDirectoryDataSource.schoolDirectoryEntryResource
                                .getSchoolDirectoryEntryByUrl(schoolUrl).dataOrNull()
                        ) { "No school found for $schoolUrl" }
                        NavCommand.Navigate(LoginScreen.create(schoolUrl))
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Napier.w(message = "ConnectLearningSpaceViewModel: could not open link", throwable = e)
                _uiState.update {
                    it.copy(errorMessage = StringResourceUiText(Res.string.invalid_url))
                }
            }
        }
    }

    fun onClickScanQRBadge() {
        _navCommandFlow.tryEmit(NavCommand.Navigate(ScanQRCode()))
    }

    fun onClickManageSchoolDirectories() {
        _navCommandFlow.tryEmit(NavCommand.Navigate(SchoolDirectoryList.create()))
    }

}
