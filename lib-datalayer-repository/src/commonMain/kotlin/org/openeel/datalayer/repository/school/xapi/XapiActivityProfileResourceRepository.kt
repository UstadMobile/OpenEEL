package org.openeel.datalayer.repository.school.xapi

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.openeel.datalayer.repository.ext.copyToValidateOnRemote
import org.openeel.datalayer.repository.flow.asRepoFlow
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.lib.dataloadstate.datetime.toInstant
import org.openeel.lib.dataloadstate.ext.copyLoadState
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.dataloadstate.ext.takeIfShouldUpdateLocal
import org.openeel.lib.xapi.ext.encodeToXapiDocument
import org.openeel.lib.xapi.ext.getJson
import org.openeel.lib.xapi.ext.isJson
import org.openeel.lib.xapi.ext.jsonKeys
import org.openeel.lib.xapi.ext.toParametersFormUrlEncoded
import org.openeel.lib.xapi.model.XapiDocument
import org.openeel.lib.xapi.remotewritequeue.XapiRemoteWriteQueue
import org.openeel.lib.xapi.remotewritequeue.XapiRemoteWriteQueueItem
import org.openeel.lib.xapi.resources.XapiActivityProfileResource
import org.openeel.lib.xapi.resources.XapiDocumentResource
import org.openeel.lib.xapi.resources.local.XapiActivityProfileResourceLocal
import kotlin.time.Clock
import kotlin.time.Instant

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

        //If it is found locally, but not found on the remote, it might not have synced yet, or it
        // might be deleted. We could check and see if there is a matching item in the remote
        // write queue, if yes, leave it alone. If no, check for deletions.

        //If not found on remote then it might have been deleted, or might not have synced there yet.
        if(localState is DataReadyState &&
            (remoteState as? NoDataLoadedState)?.reason == NoDataLoadedState.Reason.NOT_FOUND &&
            params.profileId != XapiDocumentResource.PROFILE_ID_DELETIONS &&
            !remoteWriteQueue.isPending(
                XapiRemoteWriteQueueItem.Resource.ACTIVITY_PROFILE,
                params.toParameters().toParametersFormUrlEncoded(),
            )
        ) {
            val deletedProfileIdsDoc = getJson(
                docParams = params.copy(profileId = XapiDocumentResource.PROFILE_ID_DELETIONS),
                json = json,
                deserializer = JsonObject.serializer(),
            )
            val timeDeleted = deletedProfileIdsDoc.dataOrNull()?.get(params.profileId)?.jsonPrimitive?.
                contentOrNull?.let { Instant.parse(it) } ?: Instant.DISTANT_PAST
            if(timeDeleted > localState.data.updated.toInstant()) {
                local.delete(params)
                return local.get(params, dataLoadParams)
            }
        }

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
        val deletedProfileDoc = json.encodeToXapiDocument(
            serializer = JsonObject.serializer(),
            value = buildJsonObject {
                put(params.profileId, JsonPrimitive(Clock.System.now().toString()))
            },
        )
        val deletedProfileIdParams = params.copy(
            profileId = XapiDocumentResource.PROFILE_ID_DELETIONS
        )

        local.delete(params)
        local.post(deletedProfileIdParams, deletedProfileDoc)

        remoteWriteQueue.add(
            listOf(
                XapiRemoteWriteQueueItem(
                    method = XapiRemoteWriteQueueItem.Method.DELETE,
                    resource = XapiRemoteWriteQueueItem.Resource.ACTIVITY_PROFILE,
                    itemId = params.toParameters().toParametersFormUrlEncoded(),
                ),
                XapiRemoteWriteQueueItem(
                    method = XapiRemoteWriteQueueItem.Method.POST,
                    resource = XapiRemoteWriteQueueItem.Resource.ACTIVITY_PROFILE,
                    itemId = deletedProfileIdParams.toParameters().toParametersFormUrlEncoded(),
                )
            )
        )
    }
}