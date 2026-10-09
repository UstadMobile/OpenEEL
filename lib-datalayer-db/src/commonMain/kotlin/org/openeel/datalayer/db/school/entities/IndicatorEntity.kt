package world.respect.datalayer.db.realm.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class IndicatorEntity(
    @PrimaryKey
    val indicatorId: String = "",
    val name: String = "",
    val description: String = "",
    val type: String = "",
    val sql: String = "",
)