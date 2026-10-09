package world.respect.datalayer.db.school.adapters

import world.respect.datalayer.db.school.entities.AuthTokenEntity
import world.respect.datalayer.school.model.AuthToken
import world.respect.datalayer.school.model.DeviceInfo
import world.respect.libutil.ext.randomString

fun AuthToken.toEntity(
    pGuid: String,
    pGuidHash: Long,
    code: String = randomString(32),
    deviceInfo: DeviceInfo? = null,
): AuthTokenEntity {
    return AuthTokenEntity(
        atPGuidHash = pGuidHash,
        atPGuid = pGuid,
        atCode = code,
        atToken = accessToken,
        atTimeCreated = timeCreated,
        atTtl =  ttl,
        atPlatform = deviceInfo?.platform?.pName,
        atAndroidSdkInt = deviceInfo?.androidSdkInt,
        atVersion = deviceInfo?.version,
        atManufacturer = deviceInfo?.manufacturer,
        atModel = deviceInfo?.model,
        atRam = deviceInfo?.ram,
    )
}

fun AuthTokenEntity.toModel() : AuthToken {
    return AuthToken(
        accessToken = atToken,
        timeCreated = atTimeCreated,
        ttl = atTtl
    )
}
