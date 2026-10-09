package world.respect.datalayer.repository.school.xapi

import io.ktor.server.routing.route
import kotlinx.serialization.json.Json
import org.openeel.libxapi.test.XapiAgentProfileTestParams
import world.respect.datalayer.http.server.XapiAgentProfileResourceRoute
import world.respect.lib.xapi.auth.GetAuthenticatedXapiAgentsUseCase
import world.respect.lib.xapi.resources.XapiAgentProfileResource
import world.respect.lib.xapi.resources.XapiResource
import kotlin.test.Test

class XapiAgentProfileResourceRepositoryIntegrationTest : AbstractXapiDocumentResourceRepositoryIntegrationTest<
    XapiAgentProfileResource.MultiDocParams, XapiAgentProfileResource.SingleDocumentParams, XapiAgentProfileResource
>() {

    private val json = Json

    override fun XapiResource.getTestResource(): XapiAgentProfileResource = agentProfile

    override suspend fun withEmbeddedServerAndRepoClients(
        getAuthenticatedXapiAgentsUseCase: GetAuthenticatedXapiAgentsUseCase,
        block: suspend RepositoryTestContext.() -> Unit
    ) {
        withEmbeddedServerAndRepositoryClients(
            workDir = temporaryFolder.newFolder(),
            getAuthenticatedXapiAgentsUseCase = getAuthenticatedXapiAgentsUseCase,
            routingConfig = { serverContext ->
                route("agents") {
                    XapiAgentProfileResourceRoute(
                        agentProfileResource = {
                            serverContext.datasourceContext.datasource.xapiResource.agentProfile
                        },
                        json = json,
                    )
                }
            },
            block = block,
        )
    }


    @Test
    fun givenDocumentPostedOnClient_whenGetFlowOnServerCollected_thenIsCollected() {
        givenDocumentPostedOnClient_whenGetFlowOnServerCollected_thenIsCollected(
            params = XapiAgentProfileTestParams.SINGLE_DOC_PARAMS1,
            document = XapiAgentProfileTestParams.DOC,
            authenticatedXapiAgents = { listOf(XapiAgentProfileTestParams.SINGLE_DOC_PARAMS1.agent) },
        )
    }

    @Test
    fun givenDocumentedPostedOnServer_whenGetCalledOnClient_thenMatches() {
        givenDocumentedPostedOnServer_whenGetCalledOnClient_thenMatches(
            params = XapiAgentProfileTestParams.SINGLE_DOC_PARAMS1,
            document = XapiAgentProfileTestParams.DOC,
            authenticatedXapiAgents = { listOf(XapiAgentProfileTestParams.SINGLE_DOC_PARAMS1.agent) },
        )
    }

    @Test
    fun givenDocumentPostedOnServer_whenGetAsFlowCalledOnClient_thenFlowReceivesMatchingData() {
        givenDocumentPostedOnServer_whenGetAsFlowCalledOnClient_thenFlowReceivesMatchingData(
            params = XapiAgentProfileTestParams.SINGLE_DOC_PARAMS1,
            document = XapiAgentProfileTestParams.DOC,
            authenticatedXapiAgents = { listOf(XapiAgentProfileTestParams.SINGLE_DOC_PARAMS1.agent) },
        )
    }
}
