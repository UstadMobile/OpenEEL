package org.openeel.shared.domain.launchers

interface LaunchSendWhatsAppUseCase {
    suspend operator fun invoke(phoneNumber: String)
}

