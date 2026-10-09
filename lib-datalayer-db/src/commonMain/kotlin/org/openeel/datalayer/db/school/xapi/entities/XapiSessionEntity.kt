package world.respect.datalayer.db.school.xapi.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents an xAPI session. U
 *
 * @param xseAuth - the expected authorization - a randomly generated password to use for basic auth.
 *        This is the password component ONLY. The client should send xseUid:xseAuth base64 encoded
 *        so that the server can then lookup the session and validate the auth.
 */
@Entity
@Serializable
data class XapiSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val xseUid: Long = 0,

    val xseRegistrationHi: Long = 0,

    val xseRegistrationLo: Long = 0,

    val xseAccountPersonUid: String,

    val xseActorUid: Long = 0,

    val xseStartTime: Long = 0L,

    val xseExpireTime: Long = Long.MAX_VALUE,

    val xseAuth: String,

) {
    companion object {
        const val TABLE_ID = 400122
    }
}