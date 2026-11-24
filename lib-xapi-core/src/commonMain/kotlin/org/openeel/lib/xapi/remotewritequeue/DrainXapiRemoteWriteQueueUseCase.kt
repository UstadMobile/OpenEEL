package org.openeel.lib.xapi.remotewritequeue

import io.ktor.http.HttpStatusCode
import io.ktor.http.parseQueryString
import kotlinx.serialization.json.Json
import org.openeel.lib.dataloadstate.DataErrorResult
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.dataloadstate.throwable.ExceptionWithHttpStatusCode
import org.openeel.lib.xapi.ext.paramsAdapter
import org.openeel.lib.xapi.resources.ISingleDocumentParams
import org.openeel.lib.xapi.resources.XapiDocumentResource
import org.openeel.lib.xapi.resources.XapiResource
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

    private suspend fun <MultiDocParams: Any, SingleDocParams: ISingleDocumentParams, T: XapiDocumentResource<MultiDocParams, SingleDocParams>> T.processDocumentQueueItem(
        remote: T,
        queueItem: XapiRemoteWriteQueueItem
    ) {
        val params = paramsAdapter().singleDocParamsFromParameters(
            parseQueryString(queueItem.itemId), json,
        )

        when(queueItem.method) {
            XapiRemoteWriteQueueItem.Method.POST -> {
                val document = this.get(params).dataOrNull() ?: return
                remote.post(params = params, document = document)
            }

            XapiRemoteWriteQueueItem.Method.PUT -> {
                val document = this.get(params).dataOrNull() ?: return
                remote.put(params, document)
            }

            XapiRemoteWriteQueueItem.Method.DELETE -> {
                remote.delete(params)
            }
        }

        xapiRemoteWriteQueue.markSent(listOf(queueItem.xrqItemId))
    }

    /**
     * Throw if the remote did not store the statement(s), so the queue item stays pending and will
     * be retried. A 409 Conflict means the statement is already stored (e.g. the response to an
     * earlier attempt was lost).
     */
    private fun DataLoadState<*>.requireStoredOnRemote() {
        val error = (this as? DataErrorResult)?.error
        val isConflict = (error as? ExceptionWithHttpStatusCode)?.statusCode == HttpStatusCode.Conflict.value

        if(this !is DataReadyState && !isConflict) {
            throw error ?: IllegalStateException("Statement not stored on remote: $this")
        }
    }

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
                        ).dataOrNull()?.statements?.firstOrNull()
                            ?: throw IllegalStateException("Statement ${queueItem.itemId} not found locally")

                        remoteDataSource.statements.post(listOf(statement)).requireStoredOnRemote()
                        xapiRemoteWriteQueue.markSent(listOf(queueItem.xrqItemId))
                    }

                    XapiRemoteWriteQueueItem.Resource.ACTIVITY_PROFILE -> {
                        localDataSource.activityProfile.processDocumentQueueItem(
                            remote = remoteDataSource.activityProfile,
                            queueItem = queueItem,
                        )
                    }

                    XapiRemoteWriteQueueItem.Resource.STATE -> {
                        localDataSource.state.processDocumentQueueItem(
                            remote = remoteDataSource.state,
                            queueItem = queueItem,
                        )
                    }

                    XapiRemoteWriteQueueItem.Resource.AGENT_PROFILE -> {
                        localDataSource.agentProfile.processDocumentQueueItem(
                            remote = remoteDataSource.agentProfile,
                            queueItem = queueItem,
                        )
                    }
                }

            }
        }while (pendingItems.isNotEmpty())
    }

    companion object {

        const val DEFAULT_BATCH_SIZE = 10
    }

}