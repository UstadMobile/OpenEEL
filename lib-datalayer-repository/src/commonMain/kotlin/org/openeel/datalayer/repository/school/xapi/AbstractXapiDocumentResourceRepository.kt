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
import org.openeel.lib.xapi.resources.ISingleDocumentParams
import org.openeel.lib.xapi.resources.XapiDocumentResource
import org.openeel.lib.xapi.resources.local.XapiDocumentResourceLocal
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Base implementation of an offline-first repository for [XapiDocumentResource].
 *
 * @param local the local implementation of [XapiDocumentResource] (e.g. database)
 * @param remote the remote implementation of [XapiDocumentResource] (e.g. httpclient)
 * @param remoteWriteQueue [XapiRemoteWriteQueue] to use for remote writes.
 * @param json
 * @param resource [XapiRemoteWriteQueueItem.Resource] enum value for this repository.
 */
abstract class AbstractXapiDocumentResourceRepository<MultiDocParams: Any, SingleDocParams: ISingleDocumentParams<MultiDocParams>>(
    internal val local: XapiDocumentResourceLocal<MultiDocParams, SingleDocParams>,
    internal val remote: XapiDocumentResource<MultiDocParams, SingleDocParams>,
    protected val remoteWriteQueue: XapiRemoteWriteQueue,
    protected val json: Json,
    protected val resource: XapiRemoteWriteQueueItem.Resource,
): XapiDocumentResource<MultiDocParams, SingleDocParams> {

    /**
     * Create a copy of these parameters with the given id (mapped as per [idString])
     *
     * @param id the string ID to set the copy
     * @return a copy of these parameters with the id set as per the [id] argument and all other
     *         values being the same.
     */
    abstract fun SingleDocParams.copyWithId(id: String): SingleDocParams

    override suspend fun get(
        params: SingleDocParams,
        dataLoadParams: DataLoadParams
    ): DataLoadState<XapiDocument> {
        val localState = local.get(params, dataLoadParams)

        val remoteState = remote.get(
            params = params,
            dataLoadParams = dataLoadParams.copyToValidateOnRemote(localState.metaInfo)
        )

        /* If data is found locally, but not on remote, then there are three possible scenarios:
         *
         * a) Normal situation: data not downloaded locally yet, we need to fetch the data from
         *    remote and update local.
         * b) Data was written locally and has not yet been written to the remote.
         * c) The data was deleted on remote and we should delete it locally.
         */
        if(localState is DataReadyState &&
            (remoteState as? NoDataLoadedState)?.reason == NoDataLoadedState.Reason.NOT_FOUND &&
            params.idString != XapiDocumentResource.PROFILE_ID_DELETIONS
        ) {
            val deletedProfileIdsDoc = getJson(
                docParams = params.copyWithId(XapiDocumentResource.PROFILE_ID_DELETIONS),
                json = json,
                deserializer = JsonObject.serializer(),
            )
            val timeDeleted = deletedProfileIdsDoc.dataOrNull()?.get(params.idString)
                ?.jsonPrimitive?.contentOrNull?.let {
                    Instant.parse(it)
                } ?: Instant.DISTANT_PAST
            if(timeDeleted > localState.data.updated.toInstant()) {
                local.delete(params)
                return local.get(params, dataLoadParams)
            }
        }

        remoteState.takeIfShouldUpdateLocal(localState)?.also { remote ->
            /* If data has been deleted locally, but not yet on the remote side (e.g. because the
             * RemoteWriteQueue hasn't written that delete to the remote side yet), then we need to
             * avoid accidentally bringing back data that should be deleted.
             */
            val deletedProfileIdsDoc = local.takeIf {
                params.idString != XapiDocumentResource.PROFILE_ID_DELETIONS
            }?.getJson(
                docParams = params.copyWithId(XapiDocumentResource.PROFILE_ID_DELETIONS),
                json = json,
                deserializer = JsonObject.serializer(),
            )

            val timeDeletedLocally = deletedProfileIdsDoc?.dataOrNull()?.get(params.idString)
                ?.jsonPrimitive?.contentOrNull?.let {
                    Instant.parse(it)
                } ?: Instant.DISTANT_PAST

            if(remote.data.updated.toInstant() >= timeDeletedLocally) {
                local.updateLocal(params, remote.data)
                return local.get(params, dataLoadParams).copyLoadState(
                    remoteState = remoteState
                )
            }
        }

        return localState.copyLoadState(remoteState = remoteState)
    }

    override fun getAsFlow(
        params: SingleDocParams,
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
        params: SingleDocParams,
        document: XapiDocument
    ) {
        local.post(params, document)

        remoteWriteQueue.add(
            listOf(
                XapiRemoteWriteQueueItem(
                    method = XapiRemoteWriteQueueItem.Method.POST,
                    resource = resource,
                    itemId = params.toParameters(json).toParametersFormUrlEncoded(),
                    keysToPost = document.takeIf { it.isJson() }?.jsonKeys(json),
                )
            )
        )
    }

    override suspend fun put(
        params: SingleDocParams,
        document: XapiDocument
    ) {
        local.put(params, document)

        remoteWriteQueue.add(
            listOf(
                XapiRemoteWriteQueueItem(
                    method = XapiRemoteWriteQueueItem.Method.PUT,
                    resource = resource,
                    itemId = params.toParameters(json).toParametersFormUrlEncoded(),
                )
            )
        )
    }

    /**
     * Delete a document as per
     *
     * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#single-agent-or-profile-document-put--post--get--delete
     *
     * Deleting documents in a distributed offline-first environment is challenging: the document
     * to be deleted could be on hundreds of other devices that need to learn it was deleted. Even
     * after the deletion is executed on the current device, and then sent to the sever, the document
     * might still be present on other devices.
     *
     * When a deletion is made this is tracked using the deletions profile which maintains a state
     * document of deleted profiles:
     * {
     *   "deleted-profile-id-1": "2026-01-01T00:00:00Z",
     *   "deleted-profile-id-2": "2026-01-02T00:00:00Z",
     * }
     * Note: as per the JSON specification ( https://www.json.org/json-en.html ), a JsonObject is
     * an unordered collection, so the order of keys is not fixed.
     *
     * The document is used:
     * a) On the same node that made the deletion to avoid inadvertently bringing back deleted data
     *    in the time between the data being deleted locally and the deletion being actioned on the
     *    remote side.
     * b) On other nodes to determine that the given document was deleted on the remote side and
     *    should be deleted locally.
     *
     * The deletions profile itself MUST NOT be deleted. When a deletion happens a POST request is
     * made to the Document Resource with the deletion. Because as per the xAPI spec JSON POST
     * requests are merged, this results in a state document listing the deleted profiles and when
     * they were deleted.
     */
    override suspend fun delete(
        params: SingleDocParams
    ) {
        val deletedProfileDoc = json.encodeToXapiDocument(
            serializer = JsonObject.serializer(),
            value = buildJsonObject {
                put(params.idString, JsonPrimitive(Clock.System.now().toString()))
            },
        )
        val deletedProfileIdParams = params.copyWithId(
            id = XapiDocumentResource.PROFILE_ID_DELETIONS
        )

        local.delete(params)
        local.post(deletedProfileIdParams, deletedProfileDoc)

        remoteWriteQueue.add(
            listOf(
                XapiRemoteWriteQueueItem(
                    method = XapiRemoteWriteQueueItem.Method.DELETE,
                    resource = resource,
                    itemId = params.toParameters(json).toParametersFormUrlEncoded(),
                ),
                XapiRemoteWriteQueueItem(
                    method = XapiRemoteWriteQueueItem.Method.POST,
                    resource = resource,
                    itemId = deletedProfileIdParams.toParameters(json).toParametersFormUrlEncoded(),
                )
            )
        )
    }
}