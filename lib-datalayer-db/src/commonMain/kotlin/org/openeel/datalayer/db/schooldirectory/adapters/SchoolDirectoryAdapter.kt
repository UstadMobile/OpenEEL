package world.respect.datalayer.db.schooldirectory.adapters

import world.respect.datalayer.db.schooldirectory.entities.SchoolDirectoryEntity
import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.libxxhash.XXStringHasher


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
