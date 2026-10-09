package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import io.ktor.http.Url

@Entity(
    primaryKeys = ["alrrAeUidNum", "alrrLearningUnitManifestUrlHash"]
)
data class AssignmentLearningResourceRefEntity(
    val alrrAeUidNum: Long,
    val alrrLearningUnitManifestUrlHash: Long,
    val alrrLearningUnitManifestUrl: Url,
    val alrrAppManifestUrl: Url,
)
