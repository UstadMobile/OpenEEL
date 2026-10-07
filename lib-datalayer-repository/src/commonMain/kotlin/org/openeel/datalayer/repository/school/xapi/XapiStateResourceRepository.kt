package org.openeel.datalayer.repository.school.xapi

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import org.openeel.datalayer.repository.ext.copyToValidateOnRemote
import org.openeel.datalayer.repository.flow.asRepoFlow
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.ext.copyLoadState
import org.openeel.lib.dataloadstate.ext.takeIfShouldUpdateLocal
import org.openeel.lib.xapi.ext.isJson
import org.openeel.lib.xapi.ext.jsonKeys
import org.openeel.lib.xapi.ext.toParametersFormUrlEncoded
import org.openeel.lib.xapi.model.XapiDocument
import org.openeel.lib.xapi.remotewritequeue.XapiRemoteWriteQueue
import org.openeel.lib.xapi.remotewritequeue.XapiRemoteWriteQueueItem
import org.openeel.lib.xapi.resources.XapiStateResource
import org.openeel.lib.xapi.resources.local.XapiStateResourceLocal

/**
 * An offline-first repository implementation for [XapiStateResource].
 */
class XapiStateResourceRepository(
    private val local: XapiStateResourceLocal,
    private val remote: XapiStateResource,
    private val remoteWriteQueue: XapiRemoteWriteQueue,
    private val json: Json,
): XapiStateResource {

    override suspend fun getMultipleDocuments(
        params: XapiStateResource.MultiDocParams,
        dataLoadParams: DataLoadParams
    ): DataLoadState<List<String>> {
        return local.getMultipleDocuments(params, dataLoadParams)
    }

    override suspend fun get(
        params: XapiStateResource.SingleDocumentParams,
        dataLoadParams: DataLoadParams
    ): DataLoadState<XapiDocument> {
        val localState = local.get(params, dataLoadParams)

        val remoteState = remote.get(
            params = params,
            dataLoadParams = dataLoadParams.copyToValidateOnRemote(localState.metaInfo)
        )

        remoteState.takeIfShouldUpdateLocal(localState)?.also {
            local.updateLocal(params, it.data)
            return local.get(params, dataLoadParams).copyLoadState(
                remoteState = remoteState
            )
        }

        return localState.copyLoadState(remoteState = remoteState)
    }

    override fun getAsFlow(
        params: XapiStateResource.SingleDocumentParams,
        dataLoadParams: DataLoadParams,
    ): Flow<DataLoadState<XapiDocument>> {
        return local.getAsFlow(params, dataLoadParams).asRepoFlow(
            dataLoadParams = dataLoadParams,
            remoteFlow = { remoteLoadParams ->
                remote.getAsFlow(params, remoteLoadParams)
            },
            onRemoteUpdated = {
                local.updateLocal(params, it.data)
            }
        )
    }

    override suspend fun post(
        params: XapiStateResource.SingleDocumentParams,
        document: XapiDocument
    ) {
        local.post(params, document)

        remoteWriteQueue.add(
            listOf(
                XapiRemoteWriteQueueItem(
                    method = XapiRemoteWriteQueueItem.Method.POST,
                    resource = XapiRemoteWriteQueueItem.Resource.STATE,
                    itemId = params.toParameters(json).toParametersFormUrlEncoded(),
                    keysToPost = document.takeIf { it.isJson() }?.jsonKeys(json),
                )
            )
        )
    }

    override suspend fun put(
        params: XapiStateResource.SingleDocumentParams,
        document: XapiDocument
    ) {
        local.put(params, document)

        remoteWriteQueue.add(
            listOf(
                XapiRemoteWriteQueueItem(
                    method = XapiRemoteWriteQueueItem.Method.PUT,
                    resource = XapiRemoteWriteQueueItem.Resource.STATE,
                    itemId = params.toParameters(json).toParametersFormUrlEncoded(),
                )
            )
        )
    }

    override suspend fun delete(params: XapiStateResource.SingleDocumentParams) {
        local.delete(params)
        try {
            remote.delete(params)
        } catch (e: Exception) {
            // ignore network or not found errors
        }
    }
}
