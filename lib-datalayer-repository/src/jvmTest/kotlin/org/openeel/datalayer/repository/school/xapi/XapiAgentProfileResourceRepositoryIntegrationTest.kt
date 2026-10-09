package org.openeel.datalayer.repository.school.xapi

import io.ktor.server.routing.route
import kotlinx.serialization.json.Json
import org.openeel.libxapi.test.XapiAgentProfileTestParams
import org.openeel.datalayer.http.server.XapiAgentProfileResourceRoute
import org.openeel.lib.xapi.resources.XapiAgentProfileResource
import org.openeel.lib.xapi.resources.XapiResource
import kotlin.test.Test

class XapiAgentProfileResourceRepositoryIntegrationTest : AbstractXapiDocumentResourceRepositoryIntegrationTest<
    XapiAgentProfileResource.MultiDocParams, XapiAgentProfileResource.SingleDocumentParams, XapiAgentProfileResource
>() {

    private val json = Json

    override fun XapiResource.getTestResource(): XapiAgentProfileResource = agentProfile

    override suspend fun withEmbeddedServerAndRepoClients(
        block: suspend RepositoryTestContext.() -> Unit
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
            },
            block = block,
        )
    }

    @Test
    fun givenDocumentPostedOnClient_whenGetFlowOnServerCollected_thenIsCollected() {
        givenDocumentPostedOnClient_whenGetFlowOnServerCollected_thenIsCollected(
            params = XapiAgentProfileTestParams.SINGLE_DOC_PARAMS1,
            document = XapiAgentProfileTestParams.DOC
        )
    }

    @Test
    fun givenDocumentedPostedOnServer_whenGetCalledOnClient_thenMatches() {
        givenDocumentedPostedOnServer_whenGetCalledOnClient_thenMatches(
            params = XapiAgentProfileTestParams.SINGLE_DOC_PARAMS1,
            document = XapiAgentProfileTestParams.DOC
        )
    }

    @Test
    fun givenDocumentPostedOnServer_whenGetAsFlowCalledOnClient_thenFlowReceivesMatchingData() {
        givenDocumentPostedOnServer_whenGetAsFlowCalledOnClient_thenFlowReceivesMatchingData(
            params = XapiAgentProfileTestParams.SINGLE_DOC_PARAMS1,
            document = XapiAgentProfileTestParams.DOC
        )
    }
}
