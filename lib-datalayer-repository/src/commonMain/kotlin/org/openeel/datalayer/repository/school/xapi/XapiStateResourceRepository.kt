package org.openeel.datalayer.repository.school.xapi

import kotlinx.serialization.json.Json
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.xapi.remotewritequeue.XapiRemoteWriteQueue
import org.openeel.lib.xapi.remotewritequeue.XapiRemoteWriteQueueItem
import org.openeel.lib.xapi.resources.XapiStateResource
import org.openeel.lib.xapi.resources.local.XapiStateResourceLocal

/**
 * An offline-first repository implementation for [XapiStateResource].
 */
class XapiStateResourceRepository(
    local: XapiStateResourceLocal,
    remote: XapiStateResource,
    remoteWriteQueue: XapiRemoteWriteQueue,
    json: Json,
): AbstractXapiDocumentResourceRepository<
        XapiStateResource.MultiDocParams, XapiStateResource.SingleDocumentParams
>(
    local = local,
    remote = remote,
    remoteWriteQueue = remoteWriteQueue,
    json = json,
    resource = XapiRemoteWriteQueueItem.Resource.STATE,
), XapiStateResource {

    override fun XapiStateResource.SingleDocumentParams.copyWithId(id: String): XapiStateResource.SingleDocumentParams {
        return copy(stateId = id)
    }

    override suspend fun getMultipleDocuments(
        params: XapiStateResource.MultiDocParams,
        dataLoadParams: DataLoadParams
    ): DataLoadState<List<String>> {
        return local.getMultipleDocuments(params, dataLoadParams)
    }

}
