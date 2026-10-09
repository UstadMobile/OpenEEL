package world.respect.datalayer.db.school.opds.adapters

import kotlinx.serialization.json.Json
import world.respect.datalayer.db.school.opds.OpdsParentType
import world.respect.datalayer.db.school.opds.entities.ReadiumLinkEntity
import world.respect.datalayer.db.school.opds.entities.ReadiumLinkEntity.PropertyType
import world.respect.datalayer.db.shared.ext.takeIfNotEmpty
import world.respect.lib.opds.model.ReadiumLink
import world.respect.lib.primarykeygen.PrimaryKeyGenerator

/**
 * Convert a ReadiumLink to a list of database entities. Also generates entities for alternate,
 * subcollection, and children links
 */
fun ReadiumLink.asEntities(
    pkGenerator: PrimaryKeyGenerator,
    json: Json,
    opdsParentType: OpdsParentType,
    opdsParentUid: Long,
    rlePropType: PropertyType,
    rlePropFk: Long,
    rleIndex: Int,
): List<ReadiumLinkEntity> {
    val rleId = pkGenerator.nextId(ReadiumLinkEntity.TABLE_ID)

    fun List<ReadiumLink>?.subListToEntities(
        propType: PropertyType
    ): List<ReadiumLinkEntity> {
        return this?.mapIndexed { index, link ->
            link.asEntities(
                pkGenerator = pkGenerator,
                json = json,
                opdsParentType = opdsParentType,
                opdsParentUid = opdsParentUid,
                rlePropType = propType,
                rlePropFk = rleId,
                rleIndex = index,
            )
        }?.flatten() ?: emptyList()
    }

    return listOf(
        ReadiumLinkEntity(
            rleId = rleId,
            rleOpdsParentType = opdsParentType,
            rleOpdsParentUid = opdsParentUid,
            rlePropType = rlePropType,
            rlePropFk = rlePropFk,
            rleIndex = rleIndex,
            rleHref = href,
            rleRel = rel,
            rleType = type,
            rleTitle = title,
            rleTemplated = templated,
            rleProperties = properties?.let { json.encodeToString(it) },
            rleHeight = height,
            rleWidth = width,
            rleSize = size,
            rleBitrate = bitrate,
            rleDuration = duration,
            rleLanguage = language,
        )
    ) + alternate.subListToEntities(PropertyType.LINK_ALTERNATE) +
            children.subListToEntities(PropertyType.LINK_CHILDREN) +
            subcollections.subListToEntities(PropertyType.LINK_SUB_COLLECTION)
}

/**
 * Convert a list of database entities to a list of Readium Links
 */
fun List<ReadiumLinkEntity>.asModels(
    json: Json,
    propType: PropertyType,
    propFk: Long,
): List<ReadiumLink> {
    return filter {
        it.rlePropFk == propFk && it.rlePropType == propType
    }.sortedBy {
        it.rleIndex
    }.map { thisLink ->
        ReadiumLink(
            href = thisLink.rleHref,
            rel = thisLink.rleRel,
            type = thisLink.rleType,
            title = thisLink.rleTitle,
            templated = thisLink.rleTemplated,
            properties = thisLink.rleProperties?.let { json.decodeFromString(it) },
            height = thisLink.rleHeight,
            width = thisLink.rleWidth,
            size = thisLink.rleSize,
            bitrate = thisLink.rleBitrate,
            duration = thisLink.rleDuration,
            language = thisLink.rleLanguage,
            children = asModels(
                json = json,
                propType = PropertyType.LINK_CHILDREN,
                propFk = thisLink.rleId,
            ).takeIfNotEmpty(),
            alternate = asModels(
                json = json,
                propType = PropertyType.LINK_ALTERNATE,
                propFk = thisLink.rleId,
            ).takeIfNotEmpty(),
            subcollections = asModels(
                json = json,
                propType = PropertyType.LINK_SUB_COLLECTION,
                propFk = thisLink.rleId,
            ).takeIfNotEmpty(),
        )
    }
}


