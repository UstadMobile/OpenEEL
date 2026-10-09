package world.respect.lib.opds.model

import kotlinx.serialization.Serializable

/**
 * Represents a publication as per the OPDS spec:
 * https://specs.opds.io/opds-2.0.html#22-publications
 *
 * A publication can be:
 *  A Readium Web Publication
 *  or
 *  An Opds Publication
 *
 * OPDS publication schema: https://drafts.opds.io/schema/publication.schema.json
 * Readium publication schema: https://readium.org/webpub-manifest/schema/publication.schema.json
 */
@Serializable
data class Publication(
    val metadata: ReadiumMetadata,
    val links: List<ReadiumLink>,
    val images: List<ReadiumLink>? = null,
    val readingOrder: List<ReadiumLink>? = null,
    val resources: List<ReadiumLink>? = null,
    val toc: List<ReadiumLink>? = null,
): OpdsDocument {
    companion object {
        const val MEDIA_TYPE = "application/opds-publication+json"

        const val MEDIA_TYPE_READIUM_MANIFEST = "application/webpub+json"
    }
}
