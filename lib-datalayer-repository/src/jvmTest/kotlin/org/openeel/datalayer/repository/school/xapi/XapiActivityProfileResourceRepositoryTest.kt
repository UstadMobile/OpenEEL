package org.openeel.datalayer.repository.school.xapi

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.openeel.libxapi.test.AbstractXapiActivityProfileResourceTest
import org.openeel.datalayer.http.server.XapiActivityProfileResourceRoute
import org.openeel.lib.xapi.auth.GetAuthenticatedXapiAgentsUseCase
import org.openeel.lib.xapi.resources.XapiActivityProfileResource


class XapiActivityProfileResourceRepositoryTest: AbstractXapiActivityProfileResourceTest() {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    override suspend fun withXapiDocumentResource(
        authenticatedAgents: GetAuthenticatedXapiAgentsUseCase,
        block: suspend (XapiActivityProfileResource) -> Unit
    ) {
        withEmbeddedServerAndRepositoryClients(
            workDir = temporaryFolder.newFolder(),
            getAuthenticatedXapiAgentsUseCase = authenticatedAgents,
            routingConfig = { serverContext ->
                XapiActivityProfileResourceRoute(
                    activityProfileResource = {
                        serverContext.datasourceContext.datasource.xapiResource.activityProfile
                    }
                )
            }
        ) {
            block(clients.first().datasource.activityProfile)
        }
    }


}