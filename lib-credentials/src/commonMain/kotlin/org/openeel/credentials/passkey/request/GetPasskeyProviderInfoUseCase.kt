package world.respect.credentials.passkey.request

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Use case to extract passkey provider information (including name e.g. Google Password Manager and
 * icons) given the authenticatorData received during passkey creation.
 *
 * This is done using the AAGUID the Aaguid and provider name from passkey authenticator data as per
 * https://web.dev/articles/webauthn-aaguid .
 *
 * The Aaguid  is a unique identifier assigned to each model of FIDO2/WebAuthn authenticator device.
 *
 * This use case takes the authenticator data received during passkey creation and returns
 * a [PasskeyProviderInfo] containing the extracted Aaguid and the provider name.
 *
 */
interface GetPasskeyProviderInfoUseCase {


    @OptIn(ExperimentalUuidApi::class)
    data class PasskeyProviderInfo (
        val aaguid: Uuid,
        val name: String,
        val icon_light: String? = null,
        val icon_dark: String? = null,
    )

    /**
     * @param authenticatorData The base64-encoded authenticator data received during passkey creation.
     *
     * @return A [PasskeyProviderInfo] containing the Aaguid and provider name
     */
    suspend operator fun invoke(authenticatorData: String): PasskeyProviderInfo

}