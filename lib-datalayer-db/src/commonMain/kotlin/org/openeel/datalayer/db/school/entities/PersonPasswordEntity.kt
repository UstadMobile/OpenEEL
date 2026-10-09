package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Instant

@Entity
data class PersonPasswordEntity(
    @PrimaryKey
    val ppwGuidNum: Long,
    val ppwGuid: String,
    val authAlgorithm: String,
    val authEncoded: String,
    val authSalt: String,
    val authIterations: Int,
    val authKeyLen: Int,
    val ppwLastModified: Instant,
    val ppwStored: Instant,
)
