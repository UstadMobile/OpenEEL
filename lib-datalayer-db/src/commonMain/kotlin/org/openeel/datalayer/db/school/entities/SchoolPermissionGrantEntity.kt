package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Instant
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.school.model.StatusEnum

@Entity
class SchoolPermissionGrantEntity(
    val spgUid: String,
    @PrimaryKey
    val spgUidNum: Long,
    val spgStatusEnum: StatusEnum,
    val spgToRole: PersonRoleEnum,
    val spgPermissions: Long,
    val spgLastModified: Instant,
    val spgStored: Instant,
)
