package world.respect.lib.opds.model

import kotlinx.serialization.Serializable

/**
 * Represents an OPDS 2.0 Catalog Feed.
 *
 * According to the specification, an OPDS 2.0 Catalog Feed must:
 * - Contain at least one collection (navigation, publications or groups)
 * - Contain a title in its metadata
 * - Contain a reference to itself using a self link
 * For reference, see the schema: https://drafts.opds.io/schema/feed.schema.json
 */
@Serializable
data class OpdsFeed(
    val metadata: OpdsFeedMetadata,
    val links: List<ReadiumLink>,
    val publications: List<Publication>? = null,
    val navigation: List<ReadiumLink>? = null,
    val facets: List<OpdsFacet>? = null,
    val groups: List<OpdsGroup>? = null,
    val status: String = DEFAULT_STATUS,
): OpdsDocument {
    companion object {
        const val MEDIA_TYPE = "application/opds+json"
        
        // Default status value. Matches StatusEnum.ACTIVE from respect-datalayer.school.model
        const val DEFAULT_STATUS = "active"
    }
}
