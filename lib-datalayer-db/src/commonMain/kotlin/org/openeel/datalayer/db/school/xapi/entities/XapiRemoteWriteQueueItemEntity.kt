package world.respect.datalayer.db.school.xapi.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import world.respect.lib.xapi.remotewritequeue.XapiRemoteWriteQueueItem

/**
 * @param xrqAccountGuid as per AuthenticatedUserPrincipalId.guid
 */
@Entity
data class XapiRemoteWriteQueueItemEntity(
    @PrimaryKey(autoGenerate = true)
    val xrqItemId: Int = 0,
    val xrqMethod: XapiRemoteWriteQueueItem.Method,
    val xrqResource: XapiRemoteWriteQueueItem.Resource,
    val xrqEntityItemId: String,
    val xrqTimeQueued: Long = 0,
    val xrqAttemptCount: Int = 0,
    val xrqTimeWritten: Long = 0,
    val xrqAccountGuid: String,
)
