package org.openeel.shared.viewmodel.manageuser.learningspace

import androidx.lifecycle.SavedStateHandle
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.parseUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.openeel.shared.domain.school.LaunchSchoolSelfRegistrationUseCase
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.enter_link
import org.openeel.shared.generated.resources.invalid_url
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
) : OpenEelViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(EnterHostLinkUiState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.enter_link.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false,
            )
        }
    }

    fun onLinkChanged(link: String) {
        _uiState.update { it.copy(linkUrl = link, errorMessage = null) }
    }

    fun onClickNext() {
        val hostUrl = parseHostUrlOrNull(uiState.value.linkUrl) ?: run {
            _uiState.update { it.copy(errorMessage = Res.string.invalid_url.asUiText()) }
            return
        }

        launchWithLoadingIndicator(
            onShowError = { errorMessage ->
                _uiState.update { it.copy(errorMessage = errorMessage) }
            },
        ) {
            launchSchoolSelfRegistrationUseCase(hostUrl)
        }
    }

    companion object {

        fun parseHostUrlOrNull(link: String): Url? {
            val trimmed = link.trim().takeIf { it.isNotEmpty() } ?: return null
            val withProtocol = trimmed.takeIf { it.contains("://") } ?: "https://$trimmed"

            return parseUrl(withProtocol)?.takeIf {
                it.protocol in listOf(URLProtocol.HTTP, URLProtocol.HTTPS) && it.host.isNotBlank()
            }
        }
    }
}
