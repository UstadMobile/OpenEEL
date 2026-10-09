package org.openeel.datalayer.repository.school.xapi

import io.ktor.server.routing.route
import kotlinx.serialization.json.Json
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.openeel.libxapi.test.AbstractXapiAgentProfileResourceTest
import org.openeel.datalayer.http.server.XapiAgentProfileResourceRoute
import org.openeel.lib.xapi.resources.XapiAgentProfileResource

class XapiAgentProfileResourceRepositoryTest : AbstractXapiAgentProfileResourceTest() {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    private val json = Json

    override suspend fun withXapiDocumentResource(
        block: suspend (XapiAgentProfileResource) -> Unit
    ) {
        withEmbeddedServerAndRepositoryClients(
            workDir = temporaryFolder.newFolder(),
            routingConfig = { serverContext ->
                route("agents") {
                    XapiAgentProfileResourceRoute(
                        agentProfileResource = {
                            serverContext.datasourceContext.datasource.xapiResource.agentProfile
                        },
                        json = json,
                    )
                }
            }
        ) {
            block(clients.first().datasource.agentProfile)
        }
    }

}
