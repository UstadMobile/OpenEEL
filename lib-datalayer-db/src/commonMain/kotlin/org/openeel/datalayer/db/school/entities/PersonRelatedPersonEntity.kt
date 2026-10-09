package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PersonRelatedPersonEntity(
    @PrimaryKey(autoGenerate = true)
    val prpUid: Int = 0,
    val prpPersonUidNum: Long,
    val prpOtherPersonUid: String,
    val prpOtherPersonUidNum: Long,
)

