package org.openeel.shared.viewmodel.manageuser.learningspace

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.openeel.shared.domain.openexternallink.OpenExternalLinkUseCase
import org.openeel.shared.domain.school.LaunchSchoolSelfRegistrationUseCase
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.enter_link
import org.openeel.shared.generated.resources.registration_url
import org.openeel.shared.resources.UiText
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel

data class EnterHostLinkUiState(
    val linkUrl: String = "",
    val errorMessage: UiText? = null,
)

class EnterHostLinkViewModel(
    savedStateHandle: SavedStateHandle,
    private val launchSchoolSelfRegistrationUseCase: LaunchSchoolSelfRegistrationUseCase,
    private val openExternalLinkUseCase: OpenExternalLinkUseCase,
) : OpenEelViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(EnterHostLinkUiState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.registration_url.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false,
            )
        }
    }

    fun onLinkChanged(link: String) {
        _uiState.update { it.copy(linkUrl = link, errorMessage = null) }
    }

    fun onClickNext() {
        launchWithLoadingIndicator(
            onShowError = { errorMessage ->
                _uiState.update { it.copy(errorMessage = errorMessage) }
            },
        ) {
            launchSchoolSelfRegistrationUseCase(Url(uiState.value.linkUrl.trim()))
        }
    }

    fun onClickLearnMore() {
        viewModelScope.launch {
            openExternalLinkUseCase(Url(SERVER_ADMIN_INFO_URL))
        }
    }

    companion object {
        const val SERVER_ADMIN_INFO_URL = "https://github.com/UstadMobile/OpenEEL/blob/main/README.md"
    }
}
