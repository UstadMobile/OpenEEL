package world.respect.credentials.passkey.request

import io.ktor.http.Url
import io.ktor.util.encodeBase64
import world.respect.credentials.passkey.model.PublicKeyCredentialDescriptorJSON
import world.respect.credentials.passkey.model.PublicKeyCredentialRequestOptionsJSON
import world.respect.libutil.ext.randomString

/**
 * Create the Json that is used to request an (existing) passkey for sign-in.
 *
 * This matches the PublicKeyCredentialRequestOptionsJSON spec as per:
 * https://w3c.github.io/webauthn/#dictdef-publickeycredentialrequestoptionsjson
 *
 * See CreatePublicKeyCredentialCreationOptionsJsonUseCase for further details on how passkeys are
 * handled.
 */
class CreatePublicKeyCredentialRequestOptionsJsonUseCase() {

    operator fun invoke(
        allowCredentials: List<PublicKeyCredentialDescriptorJSON> = emptyList(),
        rpId: String
    ): PublicKeyCredentialRequestOptionsJSON {
        val challenge = randomString(16)

        return PublicKeyCredentialRequestOptionsJSON(
            challenge = challenge.encodeBase64(),
            timeout = PublicKeyCredentialRequestOptionsJSON.TIME_OUT_VALUE,
            rpId = rpId,
            allowCredentials = allowCredentials,
            userVerification = "required"
        )
    }

}
