package world.respect.shared.domain.launchers

interface LaunchSendWhatsAppUseCase {
    suspend operator fun invoke(phoneNumber: String)
}

