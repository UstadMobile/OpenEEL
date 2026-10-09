package org.openeel.datalayer.db.schooldirectory.adapters

import org.openeel.datalayer.db.schooldirectory.entities.SchoolDirectoryEntity
import org.openeel.datalayer.respect.model.RespectSchoolDirectory
import org.openeel.libxxhash.XXStringHasher


fun RespectSchoolDirectory.toEntity(
    xxStringHasher: XXStringHasher,
): SchoolDirectoryEntity {
    val rdUid = xxStringHasher.hash(baseUrl.toString())
    return SchoolDirectoryEntity(
        rdUid = rdUid,
        rdUrl = baseUrl,
        rdInvitePrefix = invitePrefix,
        rdName = name,
    )
}

fun SchoolDirectoryEntity.toModel(): RespectSchoolDirectory {
    return RespectSchoolDirectory(
        invitePrefix = rdInvitePrefix,
        baseUrl = rdUrl,
        name = rdName,
    )
}
