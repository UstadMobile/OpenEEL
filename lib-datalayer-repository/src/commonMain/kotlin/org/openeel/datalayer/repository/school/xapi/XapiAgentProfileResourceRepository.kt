package world.respect.datalayer.repository.school.xapi

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import world.respect.datalayer.repository.ext.copyToValidateOnRemote
import world.respect.datalayer.repository.flow.asRepoFlow
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.ext.copyLoadState
import world.respect.lib.dataloadstate.ext.takeIfShouldUpdateLocal
import world.respect.lib.xapi.ext.isJson
import world.respect.lib.xapi.ext.jsonKeys
import world.respect.lib.xapi.ext.toParametersFormUrlEncoded
import world.respect.lib.xapi.model.XapiDocument
import world.respect.lib.xapi.remotewritequeue.XapiRemoteWriteQueue
import world.respect.lib.xapi.remotewritequeue.XapiRemoteWriteQueueItem
import world.respect.lib.xapi.resources.XapiAgentProfileResource
import world.respect.lib.xapi.resources.local.XapiAgentProfileResourceLocal

/**
 * An offline-first repository implementation for [XapiAgentProfileResource].
 */
class XapiAgentProfileResourceRepository(
    private val local: XapiAgentProfileResourceLocal,
    private val remote: XapiAgentProfileResource,
    private val remoteWriteQueue: XapiRemoteWriteQueue,
    private val json: Json,
): XapiAgentProfileResource {

    override suspend fun getMultipleDocuments(
        params: XapiAgentProfileResource.MultiDocParams,
        dataLoadParams: DataLoadParams
    ): DataLoadState<List<String>> {
        return local.getMultipleDocuments(params, dataLoadParams)
    }

    override suspend fun get(
        params: XapiAgentProfileResource.SingleDocumentParams,
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
        params: XapiAgentProfileResource.SingleDocumentParams,
        dataLoadParams: DataLoadParams
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
        params: XapiAgentProfileResource.SingleDocumentParams,
        document: XapiDocument
    ) {
        local.post(params, document)

        remoteWriteQueue.add(
            listOf(
                XapiRemoteWriteQueueItem(
                    method = XapiRemoteWriteQueueItem.Method.POST,
                    resource = XapiRemoteWriteQueueItem.Resource.AGENT_PROFILE,
                    itemId = params.toParameters(json).toParametersFormUrlEncoded(),
                    keysToPost = document.takeIf { it.isJson() }?.jsonKeys(json),
                )
            )
        )
    }

    override suspend fun put(
        params: XapiAgentProfileResource.SingleDocumentParams,
        document: XapiDocument
    ) {
        local.put(params, document)

        remoteWriteQueue.add(
            listOf(
                XapiRemoteWriteQueueItem(
                    method = XapiRemoteWriteQueueItem.Method.PUT,
                    resource = XapiRemoteWriteQueueItem.Resource.AGENT_PROFILE,
                    itemId = params.toParameters(json).toParametersFormUrlEncoded(),
                )
            )
        )
    }

    override suspend fun delete(params: XapiAgentProfileResource.SingleDocumentParams) {
        local.delete(params)
        try {
            remote.delete(params)
        } catch (e: Exception) {
            // ignore network or not found errors
        }
    }
}
