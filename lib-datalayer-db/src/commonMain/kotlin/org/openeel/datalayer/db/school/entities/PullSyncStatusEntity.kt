package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import kotlin.time.Instant

@Entity(
    primaryKeys = ["pssAccountPersonUid", "pssTableId"]
)
data class PullSyncStatusEntity(
    val pssAccountPersonUid: String,
    val pssAccountPersonUidNum: Long,
    val pssTableId: Int,
    val pssLastConsistentThrough: Instant,
    val pssPermissionsLastModified: Instant,
)
