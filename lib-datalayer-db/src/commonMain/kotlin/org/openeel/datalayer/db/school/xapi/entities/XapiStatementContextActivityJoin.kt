package world.respect.datalayer.db.school.xapi.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [
        Index(value = ["scajFromStatementIdHi", "scajFromStatementIdLo"], name = "idx_statementctx_stmt_id"),
    ]
)
/**
 * Join that is used to handle ContextActivities as per:
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#2462-contextactivities-property
 *
 * @param scajFromStatementIdHi the most significant bits of the statement uuid
 * @param scajFromStatementIdLo the least significant bits of the statement uuid
 * @param scajContextType Enum based on the contextActivity property: parent, grouping,
 * category, or other
 * @param scajToActivityId the IRI id of the activity that is being referenced
 * @param scajToActivityUid for key that joins to the activity (ActivityEntity.actUid)
 */
data class XapiStatementContextActivityJoin(
    @PrimaryKey(autoGenerate = true)
    val scajUid: Long = 0,
    val scajFromStatementIdHi: Long,
    val scajFromStatementIdLo: Long,
    val scajContextType: XapiStatementContextActivityJoinTypeEnum,
    val scajToActivityUid: Long,
    val scajToActivityId: String,
)
