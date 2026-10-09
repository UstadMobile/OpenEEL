package world.respect.datalayer.db.school.opds.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.eygraber.uri.Uri
import io.ktor.http.Url

/**
 * @property opeUid auto generated primary key
 * @property opeOfeUid where this OpdsPublicationEntity is part of a feed, the UID of the feed,
 *           otherwise 0
 * @property opeOgeUid where this OpdsPublicationEntity is part of a group, the UID of the group,
 *           otherwise 0
 * @property opeIndex where this OpdsPublicationEntity is part of a feed or group, the index of this
 *           publication within the feed or group respectively.
 * @property opeLastModified the last modified date as per the http response
 */
@Entity
class OpdsPublicationEntity(
    @PrimaryKey(autoGenerate = true)
    val opeUid: Long = 0,
    val opeOfeUid: Long,
    val opeOgeUid: Long,
    val opeIndex: Int,
    val opeUrl: Url?,
    val opeUrlHash: Long,
    val opeLastModified: Long,
    val opeEtag: String?,
    val opeMdIdentifier: Uri?,
    val opeMdLanguage: List<String>?,
    val opeMdType: Uri? = null,
    val opeMdDescription: String?,
    val opeMdNumberOfPages: Int?,
    val opeMdDuration: Double?,
) {


    companion object {

        const val TABLE_ID = 12




    }
}