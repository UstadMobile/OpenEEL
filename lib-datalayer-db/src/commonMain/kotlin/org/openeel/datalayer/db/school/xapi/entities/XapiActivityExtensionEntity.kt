package world.respect.datalayer.db.school.xapi.entities

import androidx.room.Entity

/**
 * Represents Activity extension. Joined with to XapiActivityEntity.
 */
@Entity(primaryKeys = ["aeeActivityUid", "aeeKeyHash"])
data class XapiActivityExtensionEntity(
    val aeeActivityUid: Long = 0,

    val aeeKeyHash: Long = 0,

    val aeeKey: String,

    val aeeJson: String,

    val aeeLastMod: Long = 0,

)
