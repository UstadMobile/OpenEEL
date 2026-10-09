package world.respect.datalayer.db.school.xapi.entities

import androidx.room.Entity
import kotlinx.serialization.Serializable

/**
 * StatementEntityJson is split into a separate entity so that the statement data on StatementEntity
 * we often need (e.g. score, duration, etc) can be retrieved without needing to retrieve the
 * Statement's full JSON string.
 *
 * Note: there is no XapiStatementEntityJson created for a Substatement, because the exact json
 * is included in the parent statement.
 *
 * E.g. when running the course progress query, we might need to retrieve a few statements per
 * courseBlock per student, retrieving the data for 50 students could entail fetching 2000
 * statements.
 *
 * @param fullStatement the JSON for the statement as per the 'exact' representation spec of xAPI
 *        as per statement immutability rules :
 *        https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#231-statement-immutability.
 *        The canonical JSON will be generated based on the entity fields.
 */
@Entity(
    primaryKeys = ["stmtJsonIdHi", "stmtJsonIdLo"]
)
@Serializable
data class XapiStatementEntityJson(
    val stmtJsonIdHi: Long,
    val stmtJsonIdLo: Long,
    val fullStatement: String,
) {
    companion object {
        const val TABLE_ID = 602
    }
}
