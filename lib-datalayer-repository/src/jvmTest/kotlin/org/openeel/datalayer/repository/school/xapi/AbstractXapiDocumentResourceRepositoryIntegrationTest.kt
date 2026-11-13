package org.openeel.datalayer.repository.school.xapi

import app.cash.turbine.test
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.lib.dataloadstate.ext.toPrettyString
import org.openeel.lib.xapi.auth.GetAuthenticatedXapiAgentsUseCase
import org.openeel.lib.xapi.ext.copyWithIdString
import org.openeel.lib.xapi.ext.decodeFromXapiDocument
import org.openeel.lib.xapi.model.XapiDocument
import org.openeel.lib.xapi.resources.ISingleDocumentParams
import org.openeel.lib.xapi.resources.XapiDocumentResource
import org.openeel.lib.xapi.resources.XapiResource
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

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
        val testStartTime = Clock.System.now()
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
                dataLoadParams = DataLoadParams(),
            ).filterIsInstance<NoDataLoadedState<XapiDocument>>().test(
                timeout = 5.seconds,
                name = "Wait for doc to be deleted on server"
            ) {
                assertEquals(
                    expected = NoDataLoadedState.Reason.NOT_FOUND,
                    actual = awaitItem().reason
                )
                cancelAndIgnoreRemainingEvents()
            }

            serverContext.datasourceContext.datasource.xapiResource.getTestResource().getAsFlow(
                params = params.copyWithIdString(idString = XapiDocumentResource.PROFILE_ID_DELETIONS),
                dataLoadParams = DataLoadParams()
            ).filterIsInstance<DataReadyState<XapiDocument>>().test(
                timeout = 5.seconds,
                name = "Wait for delete profile doc to be updated on server"
            ) {
                val timeDeleted = Json.decodeFromXapiDocument(
                    JsonObject.serializer(), document = awaitItem().data
                )[params.idString]?.jsonPrimitive?.contentOrNull?.let {
                    Instant.parse(it)
                }

                assertNotNull(
                    actual = timeDeleted,
                    message = "Json profile id deletions should have deleted time"
                )

                val timeNow = Clock.System.now()
                assertTrue(
                    actual = timeDeleted in testStartTime..< timeNow,
                    message = "Time deleted ($timeDeleted) should be after test started " +
                            "($testStartTime) and before time of this assertion ($timeNow)",
                )

                cancelAndIgnoreRemainingEvents()
            }


            (0..1).forEach {
                val clientResponseAfterDelete = clients.first().datasource.getTestResource().get(
                    params = params, dataLoadParams = DataLoadParams(),
                )

                assertIs<NoDataLoadedState<*>>(
                    value = clientResponseAfterDelete,
                    message = "Client $it data load state should be not loaded. It is " +
                            clientResponseAfterDelete.toPrettyString(true),
                )

                assertEquals(
                    expected = NoDataLoadedState.Reason.NOT_FOUND,
                    actual = (clientResponseAfterDelete as? NoDataLoadedState)?.reason,
                    message = "Client $it data load state should be not loaded, with reason=NOT_FOUND",
                )
            }
        }
    }

}