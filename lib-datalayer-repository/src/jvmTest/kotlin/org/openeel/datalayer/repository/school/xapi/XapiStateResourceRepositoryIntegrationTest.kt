package world.respect.datalayer.repository.school.xapi

import io.ktor.server.routing.route
import kotlinx.serialization.json.Json
import org.openeel.libxapi.test.XapiStateTestParams
import world.respect.datalayer.http.server.XapiStateResourceRoute
import world.respect.lib.xapi.auth.GetAuthenticatedXapiAgentsUseCase
import world.respect.lib.xapi.resources.XapiResource
import world.respect.lib.xapi.resources.XapiStateResource
import kotlin.test.Test

class XapiStateResourceRepositoryIntegrationTest : AbstractXapiDocumentResourceRepositoryIntegrationTest<
    XapiStateResource.MultiDocParams, XapiStateResource.SingleDocumentParams, XapiStateResource
>() {

    private val json = Json

    override fun XapiResource.getTestResource(): XapiStateResource = state

    override suspend fun withEmbeddedServerAndRepoClients(
        getAuthenticatedXapiAgentsUseCase: GetAuthenticatedXapiAgentsUseCase,
        block: suspend RepositoryTestContext.() -> Unit
    ) {
        withEmbeddedServerAndRepositoryClients(
            workDir = temporaryFolder.newFolder(),
            getAuthenticatedXapiAgentsUseCase = getAuthenticatedXapiAgentsUseCase,
            routingConfig = { serverContext ->
                route("activities") {
                    XapiStateResourceRoute(
                        stateResource = {
                            serverContext.datasourceContext.datasource.xapiResource.state
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
            params = XapiStateTestParams.SINGLE_DOC_PARAMS1,
            document = XapiStateTestParams.DOC,
            authenticatedXapiAgents = { listOf(XapiStateTestParams.SINGLE_DOC_PARAMS1.agent) }
        )
    }

    @Test
    fun givenDocumentedPostedOnServer_whenGetCalledOnClient_thenMatches() {
        givenDocumentedPostedOnServer_whenGetCalledOnClient_thenMatches(
            params = XapiStateTestParams.SINGLE_DOC_PARAMS1,
            document = XapiStateTestParams.DOC,
            authenticatedXapiAgents = { listOf(XapiStateTestParams.SINGLE_DOC_PARAMS1.agent) }
        )
    }

    @Test
    fun givenDocumentPostedOnServer_whenGetAsFlowCalledOnClient_thenFlowReceivesMatchingData() {
        givenDocumentPostedOnServer_whenGetAsFlowCalledOnClient_thenFlowReceivesMatchingData(
            params = XapiStateTestParams.SINGLE_DOC_PARAMS1,
            document = XapiStateTestParams.DOC,
            authenticatedXapiAgents = { listOf(XapiStateTestParams.SINGLE_DOC_PARAMS1.agent) }
        )
    }
}


