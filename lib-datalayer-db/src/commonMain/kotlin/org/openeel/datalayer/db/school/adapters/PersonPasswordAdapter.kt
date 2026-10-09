package org.openeel.datalayer.db.school.adapters

import org.openeel.datalayer.UidNumberMapper
import org.openeel.datalayer.db.school.entities.PersonPasswordEntity
import org.openeel.datalayer.school.model.PersonPassword

fun PersonPassword.asEntity(
    uidNumberMapper: UidNumberMapper
): PersonPasswordEntity {
    return PersonPasswordEntity(
        ppwGuid = personGuid,
        ppwGuidNum = uidNumberMapper(personGuid),
        authAlgorithm = authAlgorithm,
        authEncoded = authEncoded,
        authSalt = authSalt,
        authIterations = authIterations,
        authKeyLen = authKeyLen,
        ppwLastModified = lastModified,
        ppwStored = stored,
    )
}

fun PersonPasswordEntity.asModel(): PersonPassword {
    return PersonPassword(
        personGuid = ppwGuid,
        authAlgorithm = authAlgorithm,
        authEncoded = authEncoded,
        authSalt = authSalt,
        authIterations = authIterations,
        authKeyLen = authKeyLen,
        lastModified = ppwLastModified,
        stored = ppwStored,
    )
}
