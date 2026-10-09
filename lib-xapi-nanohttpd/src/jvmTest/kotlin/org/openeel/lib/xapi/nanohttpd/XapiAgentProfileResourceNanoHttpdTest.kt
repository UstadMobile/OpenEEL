package world.respect.lib.xapi.nanohttpd

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.openeel.libxapi.test.AbstractXapiAgentProfileResourceTest
import world.respect.lib.xapi.auth.GetAuthenticatedXapiAgentsUseCase
import world.respect.lib.xapi.resources.XapiAgentProfileResource
import kotlin.test.Test
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ContentNegotiationClient

class XapiAgentProfileResourceNanoHttpdTest : AbstractXapiAgentProfileResourceTest() {

    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    val json = Json

    val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiationClient) {
            json(json = json)
        }
    }

    override suspend fun withXapiDocumentResource(
        authenticatedAgents: GetAuthenticatedXapiAgentsUseCase,
        block: suspend (XapiAgentProfileResource) -> Unit
    ) {
        withNanoHttpdXapiResource(
            dbDir = temporaryFolder.newFolder(),
            json = json,
            httpClient = httpClient,
            authenticatedXapiAgents = authenticatedAgents,
        ) {
            block(it.agentProfile)
        }
    }


    /**
     * This function exists just to tell Android Studio/IntelliJ that this is a test class,
     * without at least one function annotated test it won't show the run test option
     */
    @Test
    fun thisIsATestClass() {}

}
