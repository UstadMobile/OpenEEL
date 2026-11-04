package org.openeel.datalayer.db.school.opds.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @property ogeOfeUid OpdsFeedEntity.ofeUid (foreign key)
 * @property ogeIndex index of the group as it appears in the OpdsFeed
 * @property ogePublicationsNull true if the [org.openeel.lib.opds.model.OpdsGroup.publications]
 *           property is null
 * @property ogeNavigationsNull true if the [org.openeel.lib.opds.model.OpdsGroup.publications]
 *           property is null
 */
@Entity
data class OpdsGroupEntity(
    @PrimaryKey(autoGenerate = true)
    val ogeUid: Long = 0,
    val ogeOfeUid: Long,
    val ogeIndex: Int,
    @ColumnInfo(defaultValue = "0")
    val ogePublicationsNull: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val ogeNavigationsNull: Boolean = false,
) {

    companion object {

        const val TABLE_ID = 3

    }
}