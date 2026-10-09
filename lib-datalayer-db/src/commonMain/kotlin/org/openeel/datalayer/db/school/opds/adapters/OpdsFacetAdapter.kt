package world.respect.datalayer.db.school.opds.adapters

import kotlinx.serialization.json.Json
import world.respect.datalayer.db.school.opds.OpdsParentType
import world.respect.datalayer.db.school.opds.entities.OpdsFacetEntity
import world.respect.datalayer.db.school.opds.entities.OpdsFeedMetadataEntity
import world.respect.datalayer.db.school.opds.entities.ReadiumLinkEntity
import world.respect.lib.opds.model.OpdsFacet
import world.respect.lib.primarykeygen.PrimaryKeyGenerator

data class OpdsFacetEntities(
    val facet: OpdsFacetEntity,
    val metadata: OpdsFeedMetadataEntity,
    val links: List<ReadiumLinkEntity>,
)

/**
 * Converts an OPDS facet to a set of entities.
 * @param ofeUid the OpdsFeed uid
 */
fun OpdsFacet.asEntities(
    primaryKeyGenerator: PrimaryKeyGenerator,
    json: Json,
    ofeUid: Long,
): OpdsFacetEntities {
    val facetUid = primaryKeyGenerator.nextId(OpdsFacetEntity.TABLE_ID)

    return OpdsFacetEntities(
        facet = OpdsFacetEntity(
            ofaeUid = facetUid,
            ofaeOfeUid =ofeUid,
        ),
        metadata = metadata.asEntity(
            ofmeOfeUid = ofeUid,
            ofmePropType = OpdsFeedMetadataEntity.PropType.FACET_METADATA,
            ofmeRelUid = facetUid,
        ),
        links = links.mapIndexed { linkIndex, link ->
            link.asEntities(
                pkGenerator = primaryKeyGenerator,
                json = json,
                opdsParentType = OpdsParentType.OPDS_FEED,
                opdsParentUid = ofeUid,
                rlePropType = ReadiumLinkEntity.PropertyType.OPDS_FACET_LINKS,
                rlePropFk = facetUid,
                rleIndex = linkIndex,
            )
        }.flatten()
    )
}

fun OpdsFacetEntities.asModel(
    json: Json
): OpdsFacet {
    return OpdsFacet(
        metadata = metadata.asModel(),
        links = links.asModels(
            json = json,
            propType = ReadiumLinkEntity.PropertyType.OPDS_FACET_LINKS,
            propFk = facet.ofaeUid,
        )
    )
}

