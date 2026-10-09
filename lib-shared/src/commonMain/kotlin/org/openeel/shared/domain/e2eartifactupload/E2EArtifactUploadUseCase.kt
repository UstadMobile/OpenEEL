package world.respect.shared.domain.e2eartifactupload

import io.ktor.http.Url

/**
 * Use case that will upload artifacts to the serve (mainly the client database) during end-to-end
 * testing such that rare/edge cases can be debugged.
 */
interface E2EArtifactUploadUseCase {

    /**
     * Upload e2e artifacts for the given school url.
     *
     * @param schoolUrl the school url as per KOIN dependency injection (used for scope)
     * @param name the name to apply to artifacts - this is passed through the intent in end-to-end
     *        testing eg step1-after-admin-login and set in the Maestro flow files.
     */
    suspend operator fun invoke(schoolUrl: Url, name: String)

    companion object {

        const val ENDPOINT_DIR = "e2e-artifact-upload"

        const val ENDPOINT_RECEIVE = "receive"

        const val ENDPOINT_API_PATH = "$ENDPOINT_DIR/$ENDPOINT_RECEIVE"

        const val DEFAULT_UPLOAD_DIR_NAME = "e2e-client-artifacts"

        const val PARAM_NAME_SCHOOL_URL = "schoolUrl"

        const val PARAM_NAME_ARTIFACT_NAME = "name"

        const val LOGTAG = "E2EArtifactUpload"

    }

}
