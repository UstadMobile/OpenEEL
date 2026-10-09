package world.respect.shared.domain.e2eartifactupload

import io.ktor.http.Url

data class DbFileForUpload(
    val filename: String,
    val bytes: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DbFileForUpload

        if (filename != other.filename) return false
        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = filename.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}

interface GetDbFilesForE2EArtifactUploadUseCase {
    suspend operator fun invoke(schoolUrl: Url): DbFileForUpload?
}
