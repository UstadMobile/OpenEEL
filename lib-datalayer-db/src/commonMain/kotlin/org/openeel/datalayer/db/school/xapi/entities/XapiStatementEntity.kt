package world.respect.datalayer.db.school.xapi.entities

import androidx.room.Entity
import androidx.room.Index
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * The primary representation of an Xapi Statement in the database. Note that the full original
 * json is stored on XapiStatementEntityJson.
 *
 * @param statementIdHi the hi bits of the statement id (which is a UUID)
 * @param statementIdLo the lo bits of the statement id (which is a UUID)
 * @param resultDuration the duration of the result in ms (if provided), otherwise 0
 * @param extensionProgress Captures the progress extension ( as per
 *        https://aicc.github.io/CMI-5_Spec_Current/samples/scenarios/13-progress_usage/ ) for use
 *        to show progress in the UI. Integer between 0 and 100 as per progress extension.
 * @param statementActorUid the ActorEntity.actorUid for the actor referenced by the actor property
 * @param statementObjectType the object type of the statement as a flag : Activity, Agent, Group,
 * StatementRef, or SubStatement
 * @param statementObjectUid1 where the object type is an Activity, Agent, or Group the uid of the
 * respective entity. When a StatementRef, the most significant uuid bits (hi). When a SubStatement,
 * the statementId will be set to the same as statementIdHi
 * @param statementObjectUid2 where the object type is an Activity, Agent, or Group, then 0. When a
 * StatementRef, the least significant uuid bits (lo). When a substatement, then statementIdLo + 1
 * @param statementObjectActivityId where the object type is an Activity, the Activity ID (IRI). An
 *        Activity might or might not have a definition. If there is no definition, there will
 *        be no related entity in the XapiActivity table. The Activity ID is therefor always kept
 *        on the StatementEntity so it is available for canonical return results.
 * @param statementVerbId where a verb has any display lang map properties, it will be stored in the
 *        verb table. If not, no entry will be created or changed. The Verb ID is therefor always
 *        kept on the StatementEntity so it is available for canonical return results.
 * @param isSubStatement if true, this is a substatement which cannot be independently retrieved.
 * @param completionOrProgress Indicates whether or not the statement is completion or progress
 * (excludes xAPI statements that are progress or completion of child activities for statements
 * received over API) - e.g. the statement could be relevant to showing the progress of the learner.
 * @param contextStatementRefIdHi most significant bits of the context registration uuid
 * @param contextStatementRefIdLo least significant bits of the context registration uuid
 * @param contextRegistrationHash the xxhash64 of contextStatementRefIdHi and contextStatementRefIdLo
 *        for purposes of reporting queries where there is a need to count the number of distinct
 *        contextRegistrations. COUNT(DISTINCT...) cannot work with multiple columns. xxhash64 has
 *        an extremely low risk of collision, not enough to meaningfully alter statistics.

 * This is used as an index field so that the database can quickly filter out other types of
 * statements (e.g. statements that are not for the top level activity, dont have a score,
 * completion status, etc). Given that Statements are click stream level, there are a lot
 * of them, so this index is important to help speed up queries on table that will get big.
 *
 * This is true if the statement has a result with a non null value for result score scaled
 */
@Entity(
    primaryKeys = ["statementIdHi", "statementIdLo"],
    indices = [
        Index("stored", name = "idx_stmt_stored"),
        Index("statementActorUid", name = "idx_stmt_actor"),
        Index("statementVerbUid", name = "idx_stmt_verb"),
        Index("statementObjectUid1", name = "idx_stmt_obj1"),
        Index("stored", name = "idx_stmt_since"),
    ]
)
@Serializable
data class XapiStatementEntity(
    val statementIdHi: Long = 0,

    val statementIdLo: Long = 0,

    val statementVerbUid: Long = 0,

    val statementVerbId: String,

    //As per the spec could be Activity, Agent, Group, StatementRef, or SubStatement
    val statementObjectType: XapiStatementEntityObjectTypeEnum,

    val statementObjectActivityId: String? = null,

    val statementObjectUid1: Long = 0,

    val statementObjectUid2: Long = 0,

    val statementActorUid: Long = 0,

    val authorityActorUid: Long = 0,

    val resultCompletion: Boolean? = null,

    val resultSuccess: Boolean? = null,

    val resultScoreScaled: Float? = null,

    val resultScoreRaw: Float? = null,

    val resultScoreMin: Float? = null,

    val resultScoreMax: Float? = null,

    val resultDuration: Long? = null,

    val resultResponse: String? = null,

    val resultExtensions: String? = null,

    val timestamp: Instant? = null,

    val stored: Instant? = null,

    val contextRegistrationHi: Long = 0,

    val contextRegistrationLo: Long = 0,

    val contextRegistrationHash: Long = 0,

    val contextLanguage: String? = null,

    val contextPlatform: String? = null,

    val contextRevision: String? = null,

    val contextStatementRefIdHi: Long = 0,

    val contextStatementRefIdLo: Long = 0,

    val contextInstructorActorUid: Long = 0,

    val contextTeamActorUid: Long = 0,

    val extensionProgress: Int? = null,

    val completionOrProgress: Boolean = false,

    val isSubStatement: Boolean = false,

    val statementVersion: String? = null,

    val stmtVoid: Boolean = false,
) {
    companion object {
        const val TABLE_ID = 60

    }
}
