package world.respect.datalayer.db.school.opds.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class OpdsFacetEntity(
    @PrimaryKey(autoGenerate = true)
    val ofaeUid: Long = 0,
    val ofaeOfeUid: Long,
) {
    companion object {

        const val TABLE_ID = 16

    }
}
