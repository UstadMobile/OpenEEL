package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @property atUid primary key (auto generated)
 * @property atPGuidHash foreign key for the related PersonEntity.pGuidHash
 * @property atPGuid foreign key for the related PersonEntity.pGuid
 * @property atToken the token itself
 * @property atTimeCreated time created (in millis)
 * @property atTtl ttl (in seconds)
 */
@Entity
data class AuthTokenEntity(
    @PrimaryKey(autoGenerate = true)
    val atUid: Long = 0L,
    val atPGuidHash: Long,
    val atPGuid: String,
    val atCode: String?,
    val atToken: String,
    val atTimeCreated: Long,
    val atTtl: Int,
    val atPlatform: String? = null,
    val atAndroidSdkInt: Int? = null,
    val atVersion: String? = null,
    val atManufacturer: String? = null,
    val atModel: String? = null,
    val atRam: Long? = null,
)
