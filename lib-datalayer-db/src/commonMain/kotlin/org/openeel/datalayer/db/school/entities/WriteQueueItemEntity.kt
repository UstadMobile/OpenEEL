package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import world.respect.datalayer.school.writequeue.WriteQueueItem

/**
 *
 * @param wqiAccountGuid as per AuthenticatedUserPrincipalId.guid
 */
@Entity
class WriteQueueItemEntity(
    @PrimaryKey(autoGenerate = true)
    val wqiQueueItemId: Int = 0,
    val wqiModel: WriteQueueItem.Model,
    val wqiUid: String,
    val wqiTimeQueued: Long = 0,
    val wqiAttemptCount: Int = 0,
    val wqiTimeWritten: Long = 0,
    val wqiAccountGuid: String,
)

