package world.respect.datalayer.repository.school.xapi

import app.cash.turbine.test
import io.ktor.server.routing.route
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.openeel.libxapi.test.XapiActivityProfileTestParams
import world.respect.datalayer.http.server.XapiActivityProfileResourceRoute
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.xapi.auth.GetAuthenticatedXapiAgentsUseCase
import world.respect.lib.xapi.model.XapiDocument
import world.respect.lib.xapi.resources.ISingleDocumentParams
import world.respect.lib.xapi.resources.XapiDocumentResource
import world.respect.lib.xapi.resources.XapiResource
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.seconds

/**
 * The DocumentResourceRepositoryIntegrationTests are used to test offline-first repository
 * behavior e.g. checking that when the document is posted on the server, that repository
 * client can retrieve it, vice versa, and testing the flow.
 */
abstract class AbstractXapiDocumentResourceRepositoryIntegrationTest<
    MultiDocParams: Any,
    SingleDocParams: ISingleDocumentParams<MultiDocParams>,
    T: XapiDocumentResource<MultiDocParams, SingleDocParams>
> {

    abstract fun XapiResource.getTestResource(): T

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    //Child classes should set the routing themselves.
    abstract suspend fun withEmbeddedServerAndRepoClients(
        getAuthenticatedXapiAgentsUseCase: GetAuthenticatedXapiAgentsUseCase,
        block: suspend RepositoryTestContext.() -> Unit
    )

    fun givenDocumentPostedOnClient_whenGetFlowOnServerCollected_thenIsCollected(
        params: SingleDocParams,
        document: XapiDocument,
        authenticatedXapiAgents: GetAuthenticatedXapiAgentsUseCase,
    ) = runBlocking {
        withEmbeddedServerAndRepoClients(
            getAuthenticatedXapiAgentsUseCase = authenticatedXapiAgents
        ) {
            clients.first().datasource.getTestResource().post(
                params = params,
                document = document,
            )

            serverContext.datasourceContext.datasource.xapiResource.getTestResource().getAsFlow(
                params = params,
                dataLoadParams = DataLoadParams()
            ).mapNotNull {
                it as? DataReadyState
            }.test(timeout = 5.seconds) {
                assertNotNull(awaitItem().data)
            }
        }
    }


    fun givenDocumentedPostedOnServer_whenGetCalledOnClient_thenMatches(
        params: SingleDocParams,
        document: XapiDocument,
        authenticatedXapiAgents: GetAuthenticatedXapiAgentsUseCase,
    ) = runBlocking {
        withEmbeddedServerAndRepoClients(
            getAuthenticatedXapiAgentsUseCase = authenticatedXapiAgents
        ) {
            serverContext.datasourceContext.datasource.xapiResource.getTestResource().post(
                params = params, document = document
            )

            val clientDocLoadState = clients.first().datasource.getTestResource().get(params = params)
            assertIs<DataReadyState<XapiDocument>>(clientDocLoadState)
        }
    }

    fun givenDocumentPostedOnServer_whenGetAsFlowCalledOnClient_thenFlowReceivesMatchingData(
        params: SingleDocParams,
        document: XapiDocument,
        authenticatedXapiAgents: GetAuthenticatedXapiAgentsUseCase,
    ) = runBlocking {
        withEmbeddedServerAndRepoClients(
            getAuthenticatedXapiAgentsUseCase = authenticatedXapiAgents
        ) {
            serverContext.datasourceContext.datasource.xapiResource.getTestResource().post(
                params = params, document = document
            )

            clients.first().datasource.getTestResource().getAsFlow(
                params = params,
                dataLoadParams = DataLoadParams()
            ).filterIsInstance<DataReadyState<XapiDocument>>().test(
                timeout = 5.seconds
            ) {
                assertContentEquals(
                    expected = document.contentsAsByteArray(),
                    actual = awaitItem().data.contentsAsByteArray()
                )
            }
        }
    }



}