package world.respect.datalayer.repository.school.xapi

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import world.respect.datalayer.repository.ext.copyToValidateOnRemote
import world.respect.datalayer.repository.flow.asRepoFlow
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.ext.copyLoadState
import world.respect.lib.dataloadstate.ext.takeIfShouldUpdateLocal
import world.respect.lib.xapi.ext.isJson
import world.respect.lib.xapi.ext.jsonKeys
import world.respect.lib.xapi.ext.toParametersFormUrlEncoded
import world.respect.lib.xapi.model.XapiDocument
import world.respect.lib.xapi.remotewritequeue.XapiRemoteWriteQueue
import world.respect.lib.xapi.remotewritequeue.XapiRemoteWriteQueueItem
import world.respect.lib.xapi.resources.XapiActivityProfileResource
import world.respect.lib.xapi.resources.local.XapiActivityProfileResourceLocal

/**
 * An offline-first repository implementation for XapiActivityProfileResource.
 */
class XapiActivityProfileResourceRepository(
    private val local: XapiActivityProfileResourceLocal,
    private val remote: XapiActivityProfileResource,
    private val remoteWriteQueue: XapiRemoteWriteQueue,
    private val json: Json,
): XapiActivityProfileResource {

    /**
     * Gets the local result first, then gets the remote result (using if-not-modified-since header).
     * If remote result is NoDataLoadedState(reason=NotModified), then just return the local result.
     *
     * If the remote result returns profile ids that are not listed locally, fetch each of them
     * from remote and use the updateLocal function to store them.
     *
     * If the remote result is missing profile ids (e.g. they may have been deleted, or might not
     * yet have been synced) then check the deleted profiles for the given params.
     *  i)  If the profileId is listed in the deleted profiles doc and its time is more recent than
     *      the local modified time, delete the data profile data locally.
     *  ii) If the profileId is not listed in the deleted profiles doc or the deletion time is before
     *      the last modified time of the local data, then do nothing (eg wait for the updated data
     *      to be written to the remote data source).
     */
    override suspend fun getMultipleDocuments(
        params: XapiActivityProfileResource.MultiDocParams,
        dataLoadParams: DataLoadParams
    ): DataLoadState<List<String>> {
        return local.getMultipleDocuments(params, dataLoadParams)
    }

    override suspend fun get(
        params: XapiActivityProfileResource.SingleDocumentParams,
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
        params: XapiActivityProfileResource.SingleDocumentParams,
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
        params: XapiActivityProfileResource.SingleDocumentParams,
        document: XapiDocument
    ) {
        local.post(params, document)

        remoteWriteQueue.add(
            listOf(
                XapiRemoteWriteQueueItem(
                    method = XapiRemoteWriteQueueItem.Method.POST,
                    resource = XapiRemoteWriteQueueItem.Resource.ACTIVITY_PROFILE,
                    itemId = params.toParameters().toParametersFormUrlEncoded(),
                    keysToPost = document.takeIf { it.isJson() }?.jsonKeys(json),
                )
            )
        )
    }

    override suspend fun put(
        params: XapiActivityProfileResource.SingleDocumentParams,
        document: XapiDocument
    ) {
        local.put(params, document)

        remoteWriteQueue.add(
            listOf(
                XapiRemoteWriteQueueItem(
                    method = XapiRemoteWriteQueueItem.Method.PUT,
                    resource = XapiRemoteWriteQueueItem.Resource.ACTIVITY_PROFILE,
                    itemId = params.toParameters().toParametersFormUrlEncoded(),
                )
            )
        )
    }

    override suspend fun delete(params: XapiActivityProfileResource.SingleDocumentParams) {
        local.delete(params)
        try {
            remote.delete(params)
        } catch (e: Exception) {
            // ignore network or not found errors
        }
    }
}