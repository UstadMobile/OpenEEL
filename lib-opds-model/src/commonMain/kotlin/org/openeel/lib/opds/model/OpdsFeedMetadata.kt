package world.respect.lib.opds.model

import com.eygraber.uri.Uri
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import world.respect.lib.serializers.InstantAsISO8601
import world.respect.lib.serializers.UriStringSerializer

/**
 * OPDS Feed Metadata.
 *
 * This includes details like title, number of items, items per page, etc.
 *
 * For reference, see the schema: https://drafts.opds.io/schema/feed-metadata.schema.json
 *
 * @property modified: As per https://json-schema.org/understanding-json-schema/reference/type date-time
 *           properties are represented using RFC 3339 : a profile of ISO8601.
 */
@Serializable
data class OpdsFeedMetadata(
    @Serializable(with = UriStringSerializer::class)
    val identifier: Uri? = null,

    @SerialName("@type")
    val type: String? = null,

    val title: String,

    val subtitle: String? = null,

    val modified: InstantAsISO8601? = null,

    val description: String? = null,

    val itemsPerPage: Int? = null,

    val currentPage: Int? = null,

    val numberOfItems: Int? = null,

)