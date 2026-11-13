package org.openeel.datalayer.repository.school.xapi

import kotlinx.serialization.json.Json
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.xapi.remotewritequeue.XapiRemoteWriteQueue
import org.openeel.lib.xapi.remotewritequeue.XapiRemoteWriteQueueItem
import org.openeel.lib.xapi.resources.XapiActivityProfileResource
import org.openeel.lib.xapi.resources.local.XapiActivityProfileResourceLocal

/**
 * An offline-first repository implementation for XapiActivityProfileResource.
 */
class XapiActivityProfileResourceRepository(
    local: XapiActivityProfileResourceLocal,
    remote: XapiActivityProfileResource,
    remoteWriteQueue: XapiRemoteWriteQueue,
    json: Json
): AbstractXapiDocumentResourceRepository<
        XapiActivityProfileResource.MultiDocParams, XapiActivityProfileResource.SingleDocumentParams
>(
    local = local,
    remote = remote,
    remoteWriteQueue = remoteWriteQueue,
    json = json,
    resource = XapiRemoteWriteQueueItem.Resource.ACTIVITY_PROFILE
), XapiActivityProfileResource {

    override fun XapiActivityProfileResource.SingleDocumentParams.copyWithId(
        id: String
    ): XapiActivityProfileResource.SingleDocumentParams {
        return copy(profileId = id)
    }

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

}