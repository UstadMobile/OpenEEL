package world.respect.lib.opds.model

import kotlinx.serialization.Serializable

/**
 * Represents a Group in an OPDS 2.0 catalog.
 * A group must contain either a navigation collection or a publications collection.
 *
 * For reference, see the schema: https://drafts.opds.io/opds-2.0#25-groups
 */
@Serializable
data class OpdsGroup(
    val metadata: OpdsFeedMetadata,
    val links: List<ReadiumLink>? = null,
    val navigation: List<ReadiumLink>? = null,
    val publications: List<Publication>? = null,
)