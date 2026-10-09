package world.respect.lib.opds.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Properties that may be included as per:
 * https://readium.org/webpub-manifest/schema/link.schema.json
 *
 */
@Serializable
data class ReadiumLinkProperties(
    val page: String? = null,

    //From https://readium.org/webpub-manifest/schema/extensions/epub/properties.schema.json
    val contains: List<String>? = null,
    val layout: String? = null,

    //From https://readium.org/webpub-manifest/schema/extensions/encryption/properties.schema.json
    val encrypted: ReadiumLinkPropertiesEncrypted? = null,

    //From https://readium.org/webpub-manifest/schema/extensions/divina/properties.schema.json
    @SerialName("break-scroll-before")
    val breakScrollBefore: Boolean? = null,

    //From https://readium.org/webpub-manifest/schema/experimental/presentation/properties.schema.json
    val clipped: Boolean? = null,
    val fit: String? = null,
    val orientation: String? = null,

    //From https://drafts.opds.io/schema/properties.schema.json
    val numberOfItems: Int? = null,
    val price: OpdsPrice? = null,
    val indirectAcquisition: List<OpdsAcquisition>? = null
)