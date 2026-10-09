package world.respect.datalayer.db.schooldirectory.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.ktor.http.Url
import kotlin.time.Instant

/**
 * @property reUid the XXHash64 of the rrSelfUrl
 */
@Entity
data class SchoolDirectoryEntryEntity(
    @PrimaryKey
    val reUid: Long,
    val reSelf: Url,
    val reXapi: Url,
    val reRespectExt: Url?,
    val reRpId: String?,
    val reLastModified: Instant,
    val reStored: Instant,
    val reInDirectoryUrl: Url?,
)