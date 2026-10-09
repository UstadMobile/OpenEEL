package world.respect.datalayer.db.school.xapi.adapters

import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.xapi.entities.XapiVerbEntity
import world.respect.datalayer.db.school.xapi.entities.XapiVerbLangMapEntry
import world.respect.lib.xapi.model.XapiVerb

data class VerbEntities(
    val verbEntity: XapiVerbEntity,
    val verbLangMapEntries: List<XapiVerbLangMapEntry>,
)

fun XapiVerb.toVerbEntities(
    uidNumberMapper: UidNumberMapper,
): VerbEntities {
    val verbIri = id
    val verbUid = uidNumberMapper(verbIri)

    return VerbEntities(
        verbEntity = XapiVerbEntity(
            verbUid = verbUid,
            verbUrlId = id,
        ),
        verbLangMapEntries = display?.entries?.map {
            XapiVerbLangMapEntry(
                vlmeVerbUid = verbUid,
                vlmeEntryString = it.value,
                vlmeLangCode = it.key,
            )
        } ?: emptyList(),
    )
}

fun VerbEntities.toModel(): XapiVerb {
    return XapiVerb(
        id = verbEntity.verbUrlId,
        display = verbLangMapEntries.associate { it.vlmeLangCode to it.vlmeEntryString },
    )
}
