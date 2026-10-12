package org.openeel.datalayer.http.school.xapi

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.routing.route
import kotlinx.serialization.json.Json
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.openeel.libxapi.test.AbstractXapiActivityProfileResourceTest
import org.openeel.datalayer.http.server.XapiActivityProfileResourceRoute
import org.openeel.datalayer.school.model.AuthToken
import org.openeel.lib.test.clientservertest.withEmbeddedDataSourceServer
import org.openeel.lib.xapi.auth.GetAuthenticatedXapiAgentsUseCase
import org.openeel.lib.xapi.resources.XapiActivityProfileResource
import org.openeel.libutil.util.time.systemTimeInMillis
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
