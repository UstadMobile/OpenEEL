package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import world.respect.datalayer.school.model.PersonRoleEnum

@Entity
data class PersonRoleEntity(
    @PrimaryKey(autoGenerate = true)
    val prUid: Int = 0,
    val prPersonGuidHash: Long,
    val prIsPrimaryRole: Boolean,
    val prRoleEnum: PersonRoleEnum,
    val prBeginDate: LocalDate? = null,
    val prEndDate: LocalDate? = null,
)