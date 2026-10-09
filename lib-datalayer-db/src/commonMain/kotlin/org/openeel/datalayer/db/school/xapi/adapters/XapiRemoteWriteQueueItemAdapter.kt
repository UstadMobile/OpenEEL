package world.respect.datalayer.db.school.xapi.adapters

import world.respect.datalayer.db.school.xapi.entities.XapiRemoteWriteQueueItemEntity
import world.respect.lib.xapi.remotewritequeue.XapiRemoteWriteQueueItem
import world.respect.libutil.util.time.systemTimeInMillis

fun XapiRemoteWriteQueueItemEntity.asModel(): XapiRemoteWriteQueueItem {
    return XapiRemoteWriteQueueItem(
        xrqItemId = xrqItemId,
        method = xrqMethod,
        resource = xrqResource,
        itemId = xrqEntityItemId,
    )
}

fun XapiRemoteWriteQueueItem.asEntity(
    accountGuid: String,
    timeQueued: Long = systemTimeInMillis(),
    attemptCount: Int = 0,
    timeWritten: Long = 0,
): XapiRemoteWriteQueueItemEntity {
    return XapiRemoteWriteQueueItemEntity(
        xrqItemId = xrqItemId,
        xrqMethod = method,
        xrqResource = resource,
        xrqEntityItemId = itemId,
        xrqTimeQueued = timeQueued,
        xrqAttemptCount = attemptCount,
        xrqTimeWritten = timeWritten,
        xrqAccountGuid = accountGuid,
    )
}
