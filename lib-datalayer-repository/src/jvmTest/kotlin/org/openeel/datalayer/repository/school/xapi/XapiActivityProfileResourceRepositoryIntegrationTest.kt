package org.openeel.datalayer.repository.school.xapi

import io.ktor.server.routing.route
import org.openeel.libxapi.test.XapiActivityProfileTestParams
import org.openeel.datalayer.http.server.XapiActivityProfileResourceRoute
import org.openeel.lib.xapi.resources.XapiActivityProfileResource
import org.openeel.lib.xapi.resources.XapiResource
import kotlin.test.Test

class XapiActivityProfileResourceRepositoryIntegrationTest: AbstractXapiDocumentResourceRepositoryIntegrationTest<
    XapiActivityProfileResource.MultiDocParams, XapiActivityProfileResource.SingleDocumentParams, XapiActivityProfileResource
>() {

    override fun XapiResource.getTestResource(): XapiActivityProfileResource = activityProfile

    override suspend fun withEmbeddedServerAndRepoClients(block: suspend RepositoryTestContext.() -> Unit) {
        withEmbeddedServerAndRepositoryClients(
            workDir = temporaryFolder.newFolder(),
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
            document = XapiActivityProfileTestParams.DOC
        )
    }

    @Test
    fun givenDocumentedPostedOnServer_whenGetCalledOnClient_thenMatches() {
        givenDocumentedPostedOnServer_whenGetCalledOnClient_thenMatches(
            params = XapiActivityProfileTestParams.SINGLE_DOC_PARAMS1,
            document = XapiActivityProfileTestParams.DOC
        )
    }

    @Test
    fun givenDocumentPostedOnServer_whenGetAsFlowCalledOnClient_thenFlowReceivesMatchingData() {
        givenDocumentPostedOnServer_whenGetAsFlowCalledOnClient_thenFlowReceivesMatchingData(
            params = XapiActivityProfileTestParams.SINGLE_DOC_PARAMS1,
            document = XapiActivityProfileTestParams.DOC
        )
    }
}