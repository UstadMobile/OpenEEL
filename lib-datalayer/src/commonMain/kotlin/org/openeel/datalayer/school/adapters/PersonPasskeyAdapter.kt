package world.respect.datalayer.school.adapters

import io.ktor.util.decodeBase64Bytes
import kotlinx.serialization.json.Json
import world.respect.credentials.passkey.CreatePasskeyUseCase
import world.respect.credentials.passkey.model.ClientDataJSON
import world.respect.datalayer.school.model.PersonPasskey
import kotlin.time.Clock

fun CreatePasskeyUseCase.PasskeyCreatedResult.toPersonPasskey(
    json : Json,
    personGuid: String,
    deviceName: String,
): PersonPasskey{
    val clientDataJSONBase64 = this.authenticationResponseJSON.response.clientDataJSON
    val passkeyProviderInfo = this.passkeyProviderInfo
    val decodedBytes = clientDataJSONBase64.decodeBase64Bytes()
    val clientDataJson = json.decodeFromString<ClientDataJSON>(
        decodedBytes.decodeToString()
    )

    val timeNow = Clock.System.now()
    val response = authenticationResponseJSON.response

    return PersonPasskey(
        personGuid = personGuid,
        lastModified = timeNow,
        stored = timeNow,
        attestationObj = response.attestationObject,
        clientDataJson = response.clientDataJSON,
        originString = clientDataJson.origin,
        credentialId = authenticationResponseJSON.id,
        challengeString = clientDataJson.challenge,
        publicKey = response.publicKey,
        deviceName = deviceName,
        isRevoked = false,
        timeCreated = timeNow,
        aaguid = passkeyProviderInfo.aaguid.toString() ,
        providerName = passkeyProviderInfo.name,
        iconDark = passkeyProviderInfo.icon_dark,
        iconLight = passkeyProviderInfo.icon_light

    )
}