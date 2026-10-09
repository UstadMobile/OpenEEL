package world.respect.lib.opds.model

import com.eygraber.uri.Uri
import kotlinx.serialization.Serializable
import world.respect.lib.serializers.UriStringSerializer

/**
 * As per https://readium.org/webpub-manifest/schema/extensions/encryption/properties.schema.json
 */
@Serializable
data class ReadiumLinkPropertiesEncrypted(
    @Serializable(with = UriStringSerializer::class)
    val algorithm: Uri,
    val compression: String? = null,
    val originalLength: Int? = null,
    @Serializable(with = UriStringSerializer::class)
    val profile: Uri? = null,
    @Serializable(with = UriStringSerializer::class)
    val scheme: Uri? = null,
)