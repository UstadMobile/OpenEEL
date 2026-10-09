package world.respect.datalayer.http.school.xapi

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.routing.route
import kotlinx.serialization.json.Json
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.openeel.libxapi.test.AbstractXapiActivityProfileResourceTest
import world.respect.datalayer.http.server.XapiActivityProfileResourceRoute
import world.respect.datalayer.school.model.AuthToken
import world.respect.lib.test.clientservertest.withEmbeddedDataSourceServer
import world.respect.lib.xapi.auth.GetAuthenticatedXapiAgentsUseCase
import world.respect.lib.xapi.resources.XapiActivityProfileResource
import world.respect.libutil.util.time.systemTimeInMillis
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ContentNegotiationClient

class XapiActivityProfileResourceHttpClientTest : AbstractXapiActivityProfileResourceTest() {

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
        block: suspend (XapiActivityProfileResource) -> Unit
    ) {
        withEmbeddedDataSourceServer(
            dbDir = temporaryFolder.newFolder(),
            getAuthenticatedXapiAgentsUseCase = authenticatedAgents,
            routingConfig = { context ->
                route("activities") {
                    XapiActivityProfileResourceRoute(
                        activityProfileResource = {
                            context.datasourceContext.datasource.xapiResource.activityProfile
                        }
                    )
                }
            }
        ) {
            val activityProfileResource = XapiActivityProfileResourceHttpClient(
                xapiUrl = { this.schoolUrl },
                httpClient = httpClient,
                tokenProvider = {
                    AuthToken("secret", systemTimeInMillis(), 3600)
                },
            )
            block(activityProfileResource)
        }
    }

}
