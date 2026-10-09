package world.respect.lib.opds.model

import kotlinx.serialization.Serializable

/**
 * Represents price information for acquisition links.
 *
 * For reference, see the schema: https://drafts.opds.io/opds-2.0#53-acquisition-links
 */
@Serializable
data class OpdsPrice(
    val currency: String,  // Required field (currency code, e.g., USD, EUR)
    val value: Float       // Required field (price value)
)