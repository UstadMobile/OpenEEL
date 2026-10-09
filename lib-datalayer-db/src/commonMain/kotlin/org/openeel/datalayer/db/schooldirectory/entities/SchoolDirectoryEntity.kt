package world.respect.datalayer.db.schooldirectory.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.ktor.http.Url

/**
 * @property rdUid the xxhash64 of the rdUrl
 * @property rdUrl the directory base url
 * @param rdInvitePrefix
 */
@Entity
data class SchoolDirectoryEntity(
    @PrimaryKey
    val rdUid: Long,
    val rdUrl: Url,
    val rdInvitePrefix: String,
    val rdName: String?,
)
