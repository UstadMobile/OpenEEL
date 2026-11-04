package org.openeel.datalayer.db.school.adapters

import org.openeel.datalayer.db.school.entities.WriteQueueItemEntity
import org.openeel.datalayer.school.writequeue.WriteQueueItem

fun WriteQueueItemEntity.asModel() : WriteQueueItem{
    return WriteQueueItem(
        queueItemId = wqiQueueItemId,
        model = wqiModel,
        uid = wqiUid,
        timeQueued = wqiTimeQueued,
        attemptCount = wqiAttemptCount,
        timeWritten = wqiTimeWritten,
    )
}


fun WriteQueueItem.asEntity(
    accountGuid: String
): WriteQueueItemEntity {
    return WriteQueueItemEntity(
        wqiQueueItemId = queueItemId,
        wqiModel = model,
        wqiUid = uid,
        wqiTimeQueued = timeQueued,
        wqiAttemptCount = attemptCount,
        wqiAccountGuid = accountGuid,
        wqiTimeWritten = timeWritten,
    )
}
