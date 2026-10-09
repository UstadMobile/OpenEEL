package world.respect.lib.opds.model

import kotlinx.serialization.Serializable
import world.respect.lib.serializers.StringListSerializer

/**
 * Represents a Link Object in OPDS 2.0.
 * The href field is required, all other fields are optional.
 *
 * Schema: https://readium.org/webpub-manifest/schema/link.schema.json
 */
@Serializable
data class ReadiumLink(
    val href: String,
    @Serializable(with = StringListSerializer::class)
    val rel: List<String>? = null,
    val type: String? = null,
    val title: String? = null,
    val templated: Boolean? = null,
    val properties: ReadiumLinkProperties? = null,
    val height: Int? = null,
    val width: Int? = null,
    val size: Int? = null,
    val bitrate: Double? = null,
    val duration: Double? = null,

    //As per the spec: language can be a single string or list.
    @Serializable(with = StringListSerializer::class)
    val language: List<String>? = null,
    val alternate: List<ReadiumLink>? = null,
    val children: List<ReadiumLink>? = null,
    val subcollections: List<ReadiumLink>? = null
)
