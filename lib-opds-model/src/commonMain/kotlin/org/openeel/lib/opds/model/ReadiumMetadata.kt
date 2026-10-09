package world.respect.lib.opds.model

import com.eygraber.uri.Uri
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import world.respect.lib.serializers.StringListSerializer
import world.respect.lib.serializers.UriStringSerializer

/**
 * Represents metadata for a Publication.
 *
 * See https://drafts.opds.io/opds-2.0#52-metadata
 * Schema: https://readium.org/webpub-manifest/schema/metadata.schema.json
 */
@Serializable
data class ReadiumMetadata(
    @SerialName("@type")
    @Serializable(with = UriStringSerializer::class)
    val type: Uri? = null,
    val title: LangMap,
    val sortAs: LangMap? = null,
    val subtitle: LangMap? = null,
    @Serializable(with = UriStringSerializer::class)
    val identifier: Uri? = null,
    val modified: String? = null,
    val published: String? = null,
    @Serializable(with = StringListSerializer::class)
    val language: List<String>? = null,
    @Serializable(with = ReadiumContributorSingleItemToListTransformer::class)
    val author: List<ReadiumContributor>? = null,
    @Serializable(with = ReadiumContributorSingleItemToListTransformer::class)
    val translator: List<ReadiumContributor>? = null,
    @Serializable(with = ReadiumContributorSingleItemToListTransformer::class)
    val editor: List<ReadiumContributor>? = null,
    @Serializable(with = ReadiumContributorSingleItemToListTransformer::class)
    val artist: List<ReadiumContributor>? = null,
    @Serializable(with = ReadiumContributorSingleItemToListTransformer::class)
    val illustrator: List<ReadiumContributor>? = null,
    @Serializable(with = ReadiumContributorSingleItemToListTransformer::class)
    val letterer: List<ReadiumContributor>? = null,
    @Serializable(with = ReadiumContributorSingleItemToListTransformer::class)
    val penciler: List<ReadiumContributor>? = null,
    @Serializable(with = ReadiumContributorSingleItemToListTransformer::class)
    val colorist: List<ReadiumContributor>? = null,
    @Serializable(with = ReadiumContributorSingleItemToListTransformer::class)
    val inker: List<ReadiumContributor>? = null,
    @Serializable(with = ReadiumContributorSingleItemToListTransformer::class)
    val narrator: List<ReadiumContributor>? = null,
    @Serializable(with = ReadiumContributorSingleItemToListTransformer::class)
    val contributor: List<ReadiumContributor>? = null,
    @Serializable(with = ReadiumContributorSingleItemToListTransformer::class)
    val publisher: List<ReadiumContributor>? = null,
    @Serializable(with = ReadiumContributorSingleItemToListTransformer::class)
    val imprint: List<ReadiumContributor>? = null,
    @Serializable(with = ReadiumSubjectToListTransformer::class)
    val subject: List<ReadiumSubject>? = null,
    val description: String? = null,
    val numberOfPages: Int? = null,
    val duration: Double? = null,
)