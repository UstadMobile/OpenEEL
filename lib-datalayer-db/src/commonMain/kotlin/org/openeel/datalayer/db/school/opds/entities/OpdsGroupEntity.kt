package world.respect.datalayer.db.school.opds.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @property ogeOfeUid OpdsFeedEntity.ofeUid (foreign key)
 * @property ogeIndex index of the group as it appears in the OpdsFeed
 */
@Entity
data class OpdsGroupEntity(
    @PrimaryKey(autoGenerate = true)
    val ogeUid: Long = 0,
    val ogeOfeUid: Long,
    val ogeIndex: Int,
) {

    companion object {

        const val TABLE_ID = 3

    }
}