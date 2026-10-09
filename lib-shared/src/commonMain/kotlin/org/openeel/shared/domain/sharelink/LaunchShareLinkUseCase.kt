package world.respect.shared.domain.sharelink

interface LaunchShareLinkUseCase {
    suspend operator fun invoke(body: String)

    companion object {
        const val MIME_TYPE = "text/plain"
    }

}
