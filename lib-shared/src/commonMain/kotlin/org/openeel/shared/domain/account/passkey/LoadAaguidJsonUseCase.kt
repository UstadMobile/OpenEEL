package world.respect.shared.domain.account.passkey

import world.respect.credentials.passkey.model.AaguidProviderData


/**
 * Load the AAGUID provider JSON file (via class resources on JVM and using assets on Android). The
 * JSON file is sourced from: https://github.com/passkeydeveloper/passkey-authenticator-aaguids
 */
interface LoadAaguidJsonUseCase {
    suspend operator fun invoke(): AaguidProviderData?
} 
