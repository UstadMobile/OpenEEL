package world.respect.shared.domain.account.passkey

import com.webauthn4j.WebAuthnManager
import com.webauthn4j.converter.exception.DataConversionException
import com.webauthn4j.credential.CredentialRecord
import com.webauthn4j.credential.CredentialRecordImpl
import com.webauthn4j.data.AuthenticationParameters
import com.webauthn4j.data.AuthenticationRequest
import com.webauthn4j.data.RegistrationRequest
import com.webauthn4j.data.client.Origin
import com.webauthn4j.data.client.challenge.DefaultChallenge
import com.webauthn4j.server.ServerProperty
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.Json
import world.respect.credentials.passkey.model.AuthenticationResponseJSON
import world.respect.credentials.passkey.model.ClientDataJSON
import world.respect.credentials.passkey.model.PasskeyVerifyResult
import world.respect.credentials.passkey.request.DecodeUserHandleUseCase
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.entities.PersonPasskeyEntity
import world.respect.lib.dataloadstate.throwable.withHttpStatus
import java.util.Base64

/**
 * Use case to authenticate an AuthenticationResponseJSON provided when a user is attempting to
 * signin using a passkey: determines which user
 */
class VerifySignInWithPasskeyUseCase(
    private val schoolDb : RespectSchoolDatabase,
    private val json: Json,
    private val decodeUserHandleUseCase: DecodeUserHandleUseCase,
) {

    private val webAuthnManager: WebAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager()

    suspend operator fun invoke(
        authenticationResponseJSON: AuthenticationResponseJSON,
        rpId: String,
    ): PasskeyVerifyResult {
        val clientDataJSONBase64 = authenticationResponseJSON.response.clientDataJSON
        val decodedBytes = Base64.getDecoder().decode(clientDataJSONBase64)
        val clientDataJson = json.decodeFromString<ClientDataJSON>(decodedBytes.decodeToString())
        // Client properties
        val credentialIdByte = Base64.getUrlDecoder().decode(authenticationResponseJSON.id)
        val userHandleByte = Base64.getUrlDecoder().decode(authenticationResponseJSON.response.userHandle)

        //Should use person uid AND authenticationResponseJSON.id to find the passkey record.
        val userHandle = decodeUserHandleUseCase(
            authenticationResponseJSON.response.userHandle
                ?: throw IllegalArgumentException("User handle cannot be null")
        )

        val authenticatorDataByte = Base64.getUrlDecoder().decode(authenticationResponseJSON.response.authenticatorData)
        val clientDataJSONByte = Base64.getUrlDecoder().decode(authenticationResponseJSON.response.clientDataJSON)
        val signatureByte = Base64.getUrlDecoder().decode(authenticationResponseJSON.response.signature)

        // Server properties
        val serverOrigin = Origin(clientDataJson.origin)
        val serverChallenge = DefaultChallenge(Base64.getUrlDecoder().decode(clientDataJson.challenge))
        val tokenBindingId: ByteArray? = null
        val serverProperty = ServerProperty(serverOrigin, rpId, serverChallenge, tokenBindingId)

        // Expectations
        val allowCredentials: List<ByteArray>? = null
        val userVerificationRequired = true
        val userPresenceRequired = true

        val passkeyData = schoolDb.getPersonPasskeyEntityDao()
            .findByPersonUidAndCredentialId(
                uid = userHandle.personUidNum,
                credentialId = authenticationResponseJSON.id,
            ) ?: throw IllegalArgumentException().withHttpStatus(401)

        val credentialRecord = createCredentialRecord( passkeyData)

        val authenticationRequest = AuthenticationRequest(
            credentialIdByte,
            userHandleByte,
            authenticatorDataByte,
            clientDataJSONByte,
            null,
            signatureByte
        )

        val authenticationParameters = AuthenticationParameters(
            serverProperty,
            credentialRecord,
            allowCredentials,
            userVerificationRequired,
            userPresenceRequired
        )


        val authenticationData = try {
            webAuthnManager.parse(authenticationRequest)
        } catch (e: DataConversionException) {
            throw e.withHttpStatus(404)
        }

        return try {
            webAuthnManager.verify(authenticationData, authenticationParameters)
            PasskeyVerifyResult(
                isVerified =  true,
                personUid = passkeyData.ppPersonUidNum
            )
        } catch (e: Exception) {
            Napier.w("VerifySigninWithPasskey: Failed", e)
            PasskeyVerifyResult(isVerified = false, 0L)
        }
    }

    private fun createCredentialRecord(
        passkeyEntity: PersonPasskeyEntity,
    ): CredentialRecord {
        val attestationObject = Base64.getUrlDecoder().decode(passkeyEntity.ppAttestationObj)
        val clientDataJSON = Base64.getUrlDecoder().decode(passkeyEntity.ppClientDataJson)
        val clientExtensionJSON: String? = null
        val transports: Set<String> = setOf("internal", "hybrid")

        val registrationRequest = RegistrationRequest(
            attestationObject,
            clientDataJSON,
            clientExtensionJSON,
            transports
        )

        val registrationData =try {
            webAuthnManager.parse(registrationRequest)
        } catch (e: DataConversionException) {
            throw e.withHttpStatus(404)
        }

        // Persist CredentialRecord object, which will be used in the authentication process.
        return registrationData.attestationObject?.let { it ->
            CredentialRecordImpl(
                it,
                registrationData.collectedClientData,
                registrationData.clientExtensions,
                registrationData.transports
            )
        } ?: throw IllegalStateException("Null attestation object")
    }
}