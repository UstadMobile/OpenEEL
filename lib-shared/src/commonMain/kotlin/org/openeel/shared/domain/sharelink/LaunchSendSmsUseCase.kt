package world.respect.shared.domain.sharelink

interface LaunchSendSmsUseCase {

    suspend operator fun invoke(body: String)

    companion object {
        const val SMS_URI_SCHEME = "smsto:"
        const val EXTRA_SMS_BODY = "sms_body"
    }
}