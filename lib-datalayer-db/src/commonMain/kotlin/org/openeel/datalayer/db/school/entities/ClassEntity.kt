package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import world.respect.datalayer.school.model.StatusEnum
import kotlin.time.Instant

@Entity
data class ClassEntity(
    val cGuid: String,
    @PrimaryKey
    val cGuidHash: Long,
    val cTitle: String,
    val cStatus: StatusEnum,
    val cDescription: String?,
    val cLastModified: Instant,
    val cStored: Instant,
    val cTeacherInviteGuid: String?,
    val cStudentInviteGuid: String?,
)
