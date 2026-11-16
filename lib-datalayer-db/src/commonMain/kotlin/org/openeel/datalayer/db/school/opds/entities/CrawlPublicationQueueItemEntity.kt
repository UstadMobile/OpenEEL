package org.openeel.datalayer.db.school.opds.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.ktor.http.Url
import org.openeel.datalayer.school.opds.crawlpublications.CrawlPublicationQueueItemStatus

@Entity(
    indices = [
        Index(value = ["jobId", "itemUrl"], unique = true),
    ],
)
data class CrawlPublicationQueueItemEntity(
    @PrimaryKey(autoGenerate = true)
    val itemId: Int = 0,
    val jobId: Int,
    val itemUrl: Url,
    val status: CrawlPublicationQueueItemStatus,
)