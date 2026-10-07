package org.openeel.credentials.passkey

import io.ktor.http.Url
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import org.openeel.credentials.passkey.model.AuthenticationResponseJSON

/**
 * Sealed class that represents a password, passkey, or QR Code Badge credential
 */
@Serializable(with = RespectCredentialSerializer::class)
sealed class OpenEelCredential

@Serializable
data class OpenEelPasswordCredential(
    val username: String,
    val password: String,
): OpenEelCredential()

@Serializable
data class OpenEelPasskeyCredential(
    val passkeyWebAuthNResponse: AuthenticationResponseJSON
): OpenEelCredential()

/**
 * A QR Code Badge is used by children who are too young to remember a username and password. They
 * simply scan the badge to authenticate.
 *
 * The URL on the QR code is in the form of:
 *
 * https://school.example.org/respect_link/qr_badge/id/123125
 *
 * The URL MUST start with the school URL (so it will be possible for it to still work if scanned
 * with a camera app).
 */
@Serializable
data class OpenEelQRBadgeCredential(
    val qrCodeUrl: Url
): OpenEelCredential()

object RespectCredentialSerializer: JsonContentPolymorphicSerializer<OpenEelCredential>(
    OpenEelCredential::class
) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<OpenEelCredential> {
        val jsonObject = element.jsonObject
        return when {
            jsonObject.containsKey("username") -> OpenEelPasswordCredential.serializer()
            jsonObject.containsKey("qrCodeUrl") -> OpenEelQRBadgeCredential.serializer()
            else -> OpenEelPasskeyCredential.serializer()
        }
    }
}





