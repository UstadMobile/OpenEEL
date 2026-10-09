package world.respect.datalayer.db.schooldirectory.adapters

import androidx.room.Embedded
import androidx.room.Relation
import world.respect.datalayer.db.schooldirectory.entities.SchoolDirectoryEntryAuthOptionEntity
import world.respect.datalayer.db.schooldirectory.entities.SchoolDirectoryEntryEntity
import world.respect.datalayer.db.schooldirectory.entities.SchoolDirectoryEntryLangMapEntity
import world.respect.datalayer.db.shared.adapters.asEntities
import world.respect.datalayer.db.shared.adapters.toIModel
import world.respect.datalayer.respect.model.AuthenticationOption
import world.respect.datalayer.respect.model.BuiltinAuthOptionConfig
import world.respect.datalayer.respect.model.OpenIdAuthOptionConfig
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.respect.model.invite.AuthOptionConfigTypeEnum
import world.respect.libxxhash.XXStringHasher

data class SchoolDirectoryEntryEntities(
    @Embedded
    val school: SchoolDirectoryEntryEntity,

    @Relation(
        parentColumn = "reUid",
        entityColumn = "sdelReUid"
    )
    val langMapEntities: List<SchoolDirectoryEntryLangMapEntity>,

    @Relation(
        parentColumn = "reUid",
        entityColumn = "sdeAoRdUid"
    )
    val authOptionEntities: List<SchoolDirectoryEntryAuthOptionEntity>,

)

fun SchoolDirectoryEntry.toEntities(
    xxStringHasher: XXStringHasher,
): SchoolDirectoryEntryEntities {
    val reUid = xxStringHasher.hash(self.toString())
    return SchoolDirectoryEntryEntities(
        school = SchoolDirectoryEntryEntity(
            reUid = reUid,
            reSelf = self,
            reXapi = xapi,
            reRespectExt = respectExt,
            reRpId = rpId,
            reLastModified = lastModified,
            reStored = stored,
            reInDirectoryUrl = inDirectoryUrl,
        ),
        langMapEntities = name.asEntities { lang, region, value ->
            SchoolDirectoryEntryLangMapEntity(
                sdelReUid = reUid,
                sdelLang = lang,
                sdelRegion = region,
                sdelValue = value,
            )
        },
        authOptionEntities = authenticationOptions.map {
            SchoolDirectoryEntryAuthOptionEntity(
                sdeAoRdUid = reUid,
                sdeAoConfigType = it.provider.authType,
                sdeAoName = it.name,
                sdeAoOpenIdIssuerUrl = (it.provider as? OpenIdAuthOptionConfig)?.issuer,
            )
        }
    )
}

fun SchoolDirectoryEntryEntities.toModel() : SchoolDirectoryEntry {
    return SchoolDirectoryEntry(
        self = school.reSelf,
        xapi = school.reXapi,
        respectExt = school.reRespectExt,
        name = langMapEntities.toIModel(),
        rpId = school.reRpId,
        lastModified = school.reLastModified,
        stored = school.reStored,
        inDirectoryUrl = school.reInDirectoryUrl,
        authenticationOptions = authOptionEntities.map {
            AuthenticationOption(
                name = it.sdeAoName,
                provider = when(it.sdeAoConfigType) {
                    AuthOptionConfigTypeEnum.OPENID -> {
                        OpenIdAuthOptionConfig(
                            issuer = it.sdeAoOpenIdIssuerUrl
                                ?: throw IllegalArgumentException("SchoolDirectoryEntry.toModel: OpenID type must have issuer")
                        )
                    }

                    AuthOptionConfigTypeEnum.BUILTIN -> {
                        BuiltinAuthOptionConfig()
                    }
                }
            )
        }.ifEmpty {
            listOf(
                AuthenticationOption(
                    name = "builtin",
                    provider = BuiltinAuthOptionConfig()
                )
            )
        }
    )
}
