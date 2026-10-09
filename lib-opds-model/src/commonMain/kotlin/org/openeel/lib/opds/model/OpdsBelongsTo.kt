package world.respect.lib.opds.model

import kotlinx.serialization.Serializable

/**
 * Represents a "belongsTo" relationship, indicating series or collection membership.
 *
 * For reference, see the schema: https://drafts.opds.io/opds-2.0#52-metadata
 */
@Serializable
data class OpdsBelongsTo(
    val series: OpdsSeries? = null,         // Optional field (indicates the series)
    val collection: String? = null          // Optional field (indicates the collection)
)