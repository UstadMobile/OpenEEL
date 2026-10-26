package org.openeel.datalayer.repository.school.xapi

import app.cash.turbine.test
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.lib.xapi.auth.GetAuthenticatedXapiAgentsUseCase
import org.openeel.lib.xapi.model.XapiDocument
import org.openeel.lib.xapi.resources.ISingleDocumentParams
import org.openeel.lib.xapi.resources.XapiDocumentResource
import org.openeel.lib.xapi.resources.XapiResource
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
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
    SingleDocParams: ISingleDocumentParams,
    T: XapiDocumentResource<MultiDocParams, SingleDocParams>
> {

    abstract fun XapiResource.getTestResource(): T

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    //Child classes should set the routing themselves.
    abstract suspend fun withEmbeddedServerAndRepoClients(
        getAuthenticatedXapiAgentsUseCase: GetAuthenticatedXapiAgentsUseCase,
        numClients: Int = 1,
        block: suspend RepositoryTestContext.() -> Unit,
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
                timeout = 5.seconds,
                name = "Wait for client to receive doc from server"
            ) {
                assertContentEquals(
                    expected = document.contentsAsByteArray(),
                    actual = awaitItem().data.contentsAsByteArray()
                )
            }
        }
    }

    fun givenDocumentDeletedByFirstClient_whenGetCalledOnSecondClient_thenIsNotFound(
        params: SingleDocParams,
        document: XapiDocument,
        authenticatedXapiAgents: GetAuthenticatedXapiAgentsUseCase
    ) = runBlocking {
        withEmbeddedServerAndRepoClients(
            getAuthenticatedXapiAgentsUseCase = authenticatedXapiAgents,
            numClients = 2,
        ) {
            clients.first().datasource.getTestResource().put(
                params = params,
                document = document,
            )

            serverContext.datasourceContext.datasource.xapiResource.getTestResource().getAsFlow(
                params = params,
                dataLoadParams = DataLoadParams()
            ).filterIsInstance<DataReadyState<XapiDocument>>().test(
                timeout = 5.seconds, name = "Wait for doc written locally to be present on server"
            ) {
                awaitItem()
                cancelAndIgnoreRemainingEvents()
            }

            clients[1].datasource.getTestResource().delete(params)

            serverContext.datasourceContext.datasource.xapiResource.getTestResource().getAsFlow(
                params = params,
                dataLoadParams = DataLoadParams()
            ).filterIsInstance<NoDataLoadedState<XapiDocument>>().test(
                timeout = 5.seconds,
                name = "Wait for doc to be deleted on server"
            ) {
                assertEquals(
                    expected = NoDataLoadedState.Reason.NOT_FOUND,
                    actual = awaitItem().reason
                )
            }

            (0..1).forEach {
                val client1ResponseAfterDelete = clients.first().datasource.getTestResource().get(
                    params = params, dataLoadParams = DataLoadParams(),
                )

                assertIs<NoDataLoadedState<*>>(client1ResponseAfterDelete)

                assertEquals(
                    expected = NoDataLoadedState.Reason.NOT_FOUND,
                    actual = (client1ResponseAfterDelete as? NoDataLoadedState)?.reason,
                    message = "Client $it data load state should be not loaded, with reason=NOT_FOUND",
                )
            }
        }
    }

}