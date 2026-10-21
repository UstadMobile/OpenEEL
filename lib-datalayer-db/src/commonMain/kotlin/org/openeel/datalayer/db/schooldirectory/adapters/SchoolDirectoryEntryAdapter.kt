package org.openeel.datalayer.db.schooldirectory.adapters

import androidx.room.Embedded
import androidx.room.Relation
import org.openeel.datalayer.db.schooldirectory.entities.SchoolDirectoryEntryAuthOptionEntity
import org.openeel.datalayer.db.schooldirectory.entities.SchoolDirectoryEntryEntity
import org.openeel.datalayer.db.schooldirectory.entities.SchoolDirectoryEntryLangMapEntity
import org.openeel.datalayer.db.shared.adapters.asEntities
import org.openeel.datalayer.db.shared.adapters.toIModel
import org.openeel.datalayer.respect.model.AuthenticationOption
import org.openeel.datalayer.respect.model.BuiltinAuthOptionConfig
import org.openeel.datalayer.respect.model.OpenIdAuthOptionConfig
import org.openeel.datalayer.respect.model.SchoolDirectoryEntry
import org.openeel.datalayer.respect.model.invite.AuthOptionConfigTypeEnum
import org.openeel.libxxhash.XXStringHasher

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
        authOptionEntities = authenticationOptions.map { option ->
            val openIdConfig = option.provider as? OpenIdAuthOptionConfig
            SchoolDirectoryEntryAuthOptionEntity(
                sdeAoRdUid = reUid,
                sdeAoConfigType = option.provider.authType,
                sdeAoName = option.name,
                sdeAoOpenIdIssuerUrl = openIdConfig?.issuer,
                sdeAoOpenIdClientId = openIdConfig?.clientId,
                sdeAoOpenIdScope = openIdConfig?.scope,
                sdeAoOpenIdRedirectUri = openIdConfig?.redirectUri,
                sdeAoOpenIdPrompt = openIdConfig?.prompt,
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
                            issuer = requireNotNull(it.sdeAoOpenIdIssuerUrl) { "Missing OpenID issuer" },
                            clientId = requireNotNull(it.sdeAoOpenIdClientId) { "Missing OpenID client ID" },
                            scope = requireNotNull(it.sdeAoOpenIdScope) { "Missing OpenID scope" },
                            redirectUri = requireNotNull(it.sdeAoOpenIdRedirectUri) { "Missing OpenID redirect URI" },
                            prompt = it.sdeAoOpenIdPrompt,
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
