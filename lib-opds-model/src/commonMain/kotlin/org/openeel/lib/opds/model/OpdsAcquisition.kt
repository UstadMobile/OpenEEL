package world.respect.lib.opds.model

import kotlinx.serialization.Serializable

/**
 * Represents an Acquisition Object, indicating how a publication can be acquired.
 *
 * For reference, see the schema: http://opds-spec.org/acquisition
 */
@Serializable
data class OpdsAcquisition(
    val type: String,                      // Required field (type of acquisition)
    val child: List<OpdsAcquisition>? = null // Optional (additional acquisition links if needed)
)