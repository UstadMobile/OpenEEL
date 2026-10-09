package world.respect.shared.domain.externallink

/**
 * UseCase to extract metadata from a webpage URL.
 *
 */
interface ExtractWebPageMetadataUseCase {
    suspend operator fun invoke(url: String): WebPageMetadata
}

data class WebPageMetadata(
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
)





