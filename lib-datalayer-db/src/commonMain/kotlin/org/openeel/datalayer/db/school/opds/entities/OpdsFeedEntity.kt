package org.openeel.datalayer.db.school.opds.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.ktor.http.Url
import kotlin.time.Clock
import kotlin.time.Instant

/**
 *
 *
 * @property ofeUid the uid of the feed: will always be the xxhash of the URL
 * @property ofeUrl: the URL of the feed (as per the self link)
 * @property ofeLastModified: the time the feed was actually modified: as per OpdsFeedMetadata.modified
 *           This matches with ModelWithTimes.modified. It is a duplication of OpdsFeedMetadata.modified
 *           but makes it simpler when running the updateLocal function to check if data has changed.
 * @property ofeLastModifiedHeader the last modified header as per the http response.
 * @property ofeStored: the time the feed was stored in the database: as per ModelWithTimes.stored
 * @property ofePublicationsNull true if the [org.openeel.lib.opds.model.OpdsFeed.publications] is null
 * @property ofeNavigationsNull true if the [org.openeel.lib.opds.model.OpdsFeed.navigation] is null
 */
@Entity
data class OpdsFeedEntity(
    @PrimaryKey
    val ofeUid: Long,
    val ofeUrl: Url,
    val ofeUrlHash: Long,
    val ofeLastModified: Instant,
    val ofeLastModifiedHeader: Instant,
    val ofeEtag: String?,
    val ofeStored: Instant = Clock.System.now(),
    @ColumnInfo(defaultValue = "0")
    val ofePublicationsNull: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val ofeNavigationsNull: Boolean = false,
) {

    companion object {

        const val TABLE_ID = 27
    }

}