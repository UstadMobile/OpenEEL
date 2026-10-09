package world.respect.shared.domain.sharelink

interface LaunchSendEmailUseCase {

    data class LaunchSendEmailRequest(
        val subject: String?,
        val body: String?,
        val to: String?
    )

    suspend operator fun invoke(request: LaunchSendEmailRequest)

}