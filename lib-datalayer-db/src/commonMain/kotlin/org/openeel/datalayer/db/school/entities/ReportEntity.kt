package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ReportEntity (
    @PrimaryKey
    val rGuid: String,
    val rOwnerGuid: String,
    val rTitle: String,
    val rOptions: String,
    val rIsTemplate: Boolean = false,
    val rActive: Boolean = true,
    val rLastModified: Long,
    val rStored: Long,
)
