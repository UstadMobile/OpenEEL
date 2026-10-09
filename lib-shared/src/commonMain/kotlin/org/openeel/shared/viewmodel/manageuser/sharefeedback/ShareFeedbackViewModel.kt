package world.respect.shared.viewmodel.manageuser.sharefeedback

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.ktor.http.Url
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.shared.domain.openexternallink.OpenExternalLinkUseCase
import world.respect.shared.domain.launchers.LaunchSendWhatsAppUseCase
import world.respect.shared.domain.sharelink.LaunchSendEmailUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.send_feedback
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

class ShareFeedbackViewModel(
    savedStateHandle: SavedStateHandle,
    private val launchSendWhatsAppUseCase: LaunchSendWhatsAppUseCase,
    private val launchSendEmailUseCase: LaunchSendEmailUseCase,
    private val openExternalLinkUseCase: OpenExternalLinkUseCase,
) : RespectViewModel(savedStateHandle) {

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
