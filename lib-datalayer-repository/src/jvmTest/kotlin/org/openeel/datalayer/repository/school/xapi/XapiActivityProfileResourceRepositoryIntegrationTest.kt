package world.respect.datalayer.repository.school.xapi

import io.ktor.server.routing.route
import org.openeel.libxapi.test.XapiActivityProfileTestParams
import org.openeel.libxapi.test.XapiStateTestParams
import world.respect.datalayer.http.server.XapiActivityProfileResourceRoute
import world.respect.lib.xapi.auth.GetAuthenticatedXapiAgentsUseCase
import world.respect.lib.xapi.resources.XapiActivityProfileResource
import world.respect.lib.xapi.resources.XapiResource
import kotlin.test.Test

class XapiActivityProfileResourceRepositoryIntegrationTest: AbstractXapiDocumentResourceRepositoryIntegrationTest<
    XapiActivityProfileResource.MultiDocParams, XapiActivityProfileResource.SingleDocumentParams, XapiActivityProfileResource
>() {

    override fun XapiResource.getTestResource(): XapiActivityProfileResource = activityProfile

    override suspend fun withEmbeddedServerAndRepoClients(
        getAuthenticatedXapiAgentsUseCase: GetAuthenticatedXapiAgentsUseCase,
        block: suspend RepositoryTestContext.() -> Unit
    ) {
        withEmbeddedServerAndRepositoryClients(
            workDir = temporaryFolder.newFolder(),
            getAuthenticatedXapiAgentsUseCase = getAuthenticatedXapiAgentsUseCase,
            routingConfig = { serverContext ->
                route("activities") {
                    XapiActivityProfileResourceRoute(
                        activityProfileResource = {
                            serverContext.datasourceContext.datasource.xapiResource.activityProfile
                        }
                    )
                }
            },
            block = block,
        )
    }

    @Test
    fun givenDocumentPostedOnClient_whenGetFlowOnServerCollected_thenIsCollected() {
        givenDocumentPostedOnClient_whenGetFlowOnServerCollected_thenIsCollected(
            params = XapiActivityProfileTestParams.SINGLE_DOC_PARAMS1,
            document = XapiActivityProfileTestParams.DOC,
            authenticatedXapiAgents = { listOf(XapiStateTestParams.SINGLE_DOC_PARAMS1.agent) }
        )
    }

    @Test
    fun givenDocumentedPostedOnServer_whenGetCalledOnClient_thenMatches() {
        givenDocumentedPostedOnServer_whenGetCalledOnClient_thenMatches(
            params = XapiActivityProfileTestParams.SINGLE_DOC_PARAMS1,
            document = XapiActivityProfileTestParams.DOC,
            authenticatedXapiAgents = { listOf(XapiStateTestParams.SINGLE_DOC_PARAMS1.agent) }
        )
    }

    @Test
    fun givenDocumentPostedOnServer_whenGetAsFlowCalledOnClient_thenFlowReceivesMatchingData() {
        givenDocumentPostedOnServer_whenGetAsFlowCalledOnClient_thenFlowReceivesMatchingData(
            params = XapiActivityProfileTestParams.SINGLE_DOC_PARAMS1,
            document = XapiActivityProfileTestParams.DOC,
            authenticatedXapiAgents = { listOf(XapiStateTestParams.SINGLE_DOC_PARAMS1.agent) }
        )
    }
}