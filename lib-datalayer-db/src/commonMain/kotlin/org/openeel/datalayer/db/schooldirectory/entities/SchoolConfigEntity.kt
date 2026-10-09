package world.respect.datalayer.db.schooldirectory.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents the server configuration of a School which is hosted on this server.
 *
 * @param rcUid UID as per RealmEntity.reUid
 * @param dbUrl database URL or path for the school-specific database.
 */
@Entity
class SchoolConfigEntity(
    @PrimaryKey
    val rcUid: Long,
    val dbUrl: String,
)
