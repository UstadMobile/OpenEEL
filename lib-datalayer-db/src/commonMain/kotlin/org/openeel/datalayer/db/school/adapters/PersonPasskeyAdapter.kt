package world.respect.datalayer.db.school.adapters

import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.entities.PersonPasskeyEntity
import world.respect.datalayer.school.model.PersonPasskey

fun PersonPasskey.asEntity(
    uidNumberMapper: UidNumberMapper,
): PersonPasskeyEntity {
    return PersonPasskeyEntity(
        ppPersonUidNum = uidNumberMapper(personGuid),
        ppLastModified = lastModified,
        ppStored = stored,
        ppAttestationObj = attestationObj,
        ppClientDataJson = clientDataJson,
        ppOriginString = originString,
        ppCredentialId = credentialId,
        ppChallengeString = challengeString,
        ppPublicKey = publicKey,
        ppDeviceName = deviceName,
        ppTimeCreated = timeCreated,
        isRevoked = if (isRevoked) PersonPasskeyEntity.REVOKED else PersonPasskeyEntity.NOT_REVOKED,
        ppAaguid = aaguid ?: "",
        ppProviderName = providerName ?: "",
        ppIconDark = iconDark?:"",
        ppIconLight = iconLight?:""

    )
}

fun PersonPasskeyEntity.asModel(
    personGuid: String,
): PersonPasskey {
    return PersonPasskey(
        personGuid = personGuid,
        lastModified = ppLastModified,
        stored = ppStored,
        attestationObj = ppAttestationObj,
        clientDataJson = ppClientDataJson,
        originString = ppOriginString,
        credentialId = ppCredentialId,
        challengeString = ppChallengeString,
        publicKey = ppPublicKey,
        isRevoked = isRevoked == PersonPasskeyEntity.REVOKED,
        deviceName = ppDeviceName,
        timeCreated = ppTimeCreated,
        aaguid = ppAaguid,
        providerName = ppProviderName,
        iconDark = ppIconDark,
        iconLight = ppIconLight
    )
}
