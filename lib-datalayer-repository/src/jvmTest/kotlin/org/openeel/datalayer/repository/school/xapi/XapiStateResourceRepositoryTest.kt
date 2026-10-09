package world.respect.datalayer.repository.school.xapi

import io.ktor.server.routing.route
import kotlinx.serialization.json.Json
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.openeel.libxapi.test.AbstractXapiStateResourceTest
import world.respect.datalayer.http.server.XapiStateResourceRoute
import world.respect.lib.xapi.auth.GetAuthenticatedXapiAgentsUseCase
import world.respect.lib.xapi.resources.XapiStateResource
import kotlin.test.Test

class XapiStateResourceRepositoryTest : AbstractXapiStateResourceTest() {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    private val json = Json

    override suspend fun withXapiDocumentResource(
        authenticatedAgents: GetAuthenticatedXapiAgentsUseCase,
        block: suspend (XapiStateResource) -> Unit
    ) {
        withEmbeddedServerAndRepositoryClients(
            workDir = temporaryFolder.newFolder(),
            getAuthenticatedXapiAgentsUseCase = authenticatedAgents,
            routingConfig = { serverContext ->
                route("activities") {
                    XapiStateResourceRoute(
                        stateResource = {
                            serverContext.datasourceContext.datasource.xapiResource.state
                        },
                        json = json,
                    )
                }
            }
        ) {
            block(clients.first().datasource.state)
        }
    }

    @Test
    fun test() {

    }

}

