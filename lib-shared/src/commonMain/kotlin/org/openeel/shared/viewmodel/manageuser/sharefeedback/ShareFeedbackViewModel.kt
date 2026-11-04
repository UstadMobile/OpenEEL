package org.openeel.shared.viewmodel.manageuser.sharefeedback

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.ktor.http.Url
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.openeel.shared.domain.openexternallink.OpenExternalLinkUseCase
import org.openeel.shared.domain.launchers.LaunchSendWhatsAppUseCase
import org.openeel.shared.domain.sharelink.LaunchSendEmailUseCase
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.send_feedback
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel

class ShareFeedbackViewModel(
    savedStateHandle: SavedStateHandle,
    private val launchSendWhatsAppUseCase: LaunchSendWhatsAppUseCase,
    private val launchSendEmailUseCase: LaunchSendEmailUseCase,
    private val openExternalLinkUseCase: OpenExternalLinkUseCase,
) : OpenEelViewModel(savedStateHandle) {

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.send_feedback.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false
            )
        }
    }

    fun onClickWhatsApp() {
        viewModelScope.launch {
            launchSendWhatsAppUseCase(WHATSAPP_NUMBER)
        }
    }

    fun onClickEmail() {
        viewModelScope.launch {
            launchSendEmailUseCase(
                LaunchSendEmailUseCase.LaunchSendEmailRequest(
                    subject = EMAIL_SUBJECT,
                    body = "",
                    to = EMAIL_ADDRESS
                )
            )
        }
    }

    fun onClickPublicForum() {
        viewModelScope.launch {
            openExternalLinkUseCase(Url(FORUM_URL))
        }
    }

    companion object {
        const val EMAIL_ADDRESS = "info@ustadmobile.com"
        const val EMAIL_SUBJECT = "RESPECT Feedback"
        const val WHATSAPP_NUMBER = "+1234567890" // TODO: replace with actual number
        const val FORUM_URL = "https://github.com/UstadMobile/Respect/discussions"
    }
}
