package world.respect.lib.opds.model

import kotlinx.serialization.Serializable

/**
 * Represents a series that a publication belongs to.
 *
 * For reference, see the schema: https://drafts.opds.io/opds-2.0#52-metadata
 */
@Serializable
data class OpdsSeries(
    val name: String,                      // Required field (name of the series)
    val position: Int? = null,             // Optional field (position in the series)
    val links: List<ReadiumLink>? = null      // Optional field (links about the series)
)