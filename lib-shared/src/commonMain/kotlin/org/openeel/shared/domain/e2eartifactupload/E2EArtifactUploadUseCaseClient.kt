package world.respect.shared.domain.e2eartifactupload

import com.ustadmobile.libcache.connectivitymonitor.ConnectivityMonitor
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.takeFrom
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds

class E2EArtifactUploadUseCaseClient(
    private val httpClient: HttpClient,
    private val getDbFilesForE2EArtifactUploadUseCase: GetDbFilesForE2EArtifactUploadUseCase,
    private val connectivityMonitor: ConnectivityMonitor,
) : E2EArtifactUploadUseCase {

    override suspend fun invoke(schoolUrl: Url, name: String) {
        val file = getDbFilesForE2EArtifactUploadUseCase(schoolUrl) ?: return

        withTimeout(DEFAULT_TIMEOUT_MS.milliseconds) {
            /*
             * Wait for connection to be available. If flight mode was recently switched off this
             * might take a moment
             */
            connectivityMonitor.statusFlow.first { it.isConnected }

            httpClient.post {
                url {
                    takeFrom(schoolUrl)
                    appendPathSegments("api/${E2EArtifactUploadUseCase.ENDPOINT_API_PATH}")
                }

                parameter(E2EArtifactUploadUseCase.PARAM_NAME_ARTIFACT_NAME, name)
                contentType(ContentType.Application.OctetStream)
                setBody(file.bytes)
            }
        }
    }

    companion object {

        const val DEFAULT_TIMEOUT_MS = 20_000L

    }
}
