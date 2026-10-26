package org.openeel.lib.xapi.remotewritequeue

import io.github.aakira.napier.Napier
import io.ktor.http.parseQueryString
import kotlinx.serialization.json.Json
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.xapi.resources.XapiActivityProfileResource
import org.openeel.lib.xapi.resources.XapiAgentProfileResource
import org.openeel.lib.xapi.resources.XapiResource
import org.openeel.lib.xapi.resources.XapiStateResource
import org.openeel.lib.xapi.resources.XapiStatementsResource
import org.openeel.lib.xapi.resources.local.XapiResourceLocal
import kotlin.uuid.Uuid

/**
 *
 */
class DrainXapiRemoteWriteQueueUseCase(
    private val xapiRemoteWriteQueue: XapiRemoteWriteQueue,
    private val remoteDataSource: XapiResource,
    private val localDataSource: XapiResourceLocal,
    private val json: Json = Json,
) {

    /**
     * Drain the Xapi Remote Write queue.
     */
    suspend operator fun invoke() {
        do {
            val pendingItems = xapiRemoteWriteQueue.getPending(DEFAULT_BATCH_SIZE)
            for(queueItem in pendingItems) {
                when(queueItem.resource) {
                    XapiRemoteWriteQueueItem.Resource.STATEMENTS -> {
                        val statement = localDataSource.statements.get(
                            listParams = XapiStatementsResource.GetStatementParams(
                                statementId = Uuid.parse(queueItem.itemId),
                                format = XapiStatementsResource.GetStatementFormatEnum.EXACT,
                            )
                        ).dataOrNull()?.statements?.firstOrNull() ?: continue

                        remoteDataSource.statements.post(listOf(statement))
                        xapiRemoteWriteQueue.markSent(listOf(queueItem.xrqItemId))
                    }

                    XapiRemoteWriteQueueItem.Resource.ACTIVITY_PROFILE -> {
                        val params = XapiActivityProfileResource.SingleDocumentParams.fromParameters(
                            parseQueryString(queueItem.itemId)
                        )

                        when(queueItem.method) {
                            XapiRemoteWriteQueueItem.Method.POST -> {
                                val document = localDataSource.activityProfile.get(params).dataOrNull() ?: continue
                                remoteDataSource.activityProfile.post(params, document)
                            }

                            XapiRemoteWriteQueueItem.Method.PUT -> {
                                val document = localDataSource.activityProfile.get(params).dataOrNull() ?: continue
                                remoteDataSource.activityProfile.post(params, document)
                            }

                            XapiRemoteWriteQueueItem.Method.DELETE -> {
                                remoteDataSource.activityProfile.delete(params)
                            }
                        }

                        xapiRemoteWriteQueue.markSent(listOf(queueItem.xrqItemId))
                    }

                    XapiRemoteWriteQueueItem.Resource.STATE -> {
                        val params = XapiStateResource.SingleDocumentParams.fromParameters(
                            params = parseQueryString(queueItem.itemId),
                            json = json,
                        )

                        when(queueItem.method) {
                            XapiRemoteWriteQueueItem.Method.POST -> {
                                val document = localDataSource.state.get(params).dataOrNull() ?: continue
                                remoteDataSource.state.post(params, document)
                            }

                            XapiRemoteWriteQueueItem.Method.PUT -> {
                                val document = localDataSource.state.get(params).dataOrNull() ?: continue
                                remoteDataSource.state.put(params, document)
                            }

                            XapiRemoteWriteQueueItem.Method.DELETE -> {
                                remoteDataSource.state.delete(params)
                            }
                        }

                        xapiRemoteWriteQueue.markSent(listOf(queueItem.xrqItemId))
                    }

                    XapiRemoteWriteQueueItem.Resource.AGENT_PROFILE -> {
                        val params = XapiAgentProfileResource.SingleDocumentParams.fromParameters(
                            params = parseQueryString(queueItem.itemId),
                            json = json,
                        )

                        when(queueItem.method) {
                            XapiRemoteWriteQueueItem.Method.POST -> {
                                val document = localDataSource.agentProfile.get(params).dataOrNull() ?: continue
                                remoteDataSource.agentProfile.post(params, document)
                            }

                            XapiRemoteWriteQueueItem.Method.PUT -> {
                                val document = localDataSource.agentProfile.get(params).dataOrNull() ?: continue
                                remoteDataSource.agentProfile.put(params, document)
                            }

                            XapiRemoteWriteQueueItem.Method.DELETE -> {
                                remoteDataSource.agentProfile.delete(params)
                            }
                        }

                        xapiRemoteWriteQueue.markSent(listOf(queueItem.xrqItemId))
                    }
                }

            }
        }while (pendingItems.isNotEmpty())
    }

    companion object {

        const val DEFAULT_BATCH_SIZE = 10
    }

}