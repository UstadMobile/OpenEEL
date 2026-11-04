package org.openeel.datalayer.db.school.domain.query

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.openeel.datalayer.db.RespectSchoolDatabase
import org.openeel.datalayer.db.school.xapi.entities.XapiStatementEntity
import org.openeel.datalayer.db.school.xapi.entities.XapiStatementEntityObjectTypeEnum
import java.util.UUID
import kotlin.time.Clock

const val DEFAULT_NUM_DAYS = 7
const val DEFAULT_NUM_STATEMENTS_PER_DAY = 10
const val DEFAULT_DURATION_PER_STATEMENT = 60000L
const val DEFAULT_STATEMENT_CLAZZ_UID = 42L

data class StatementsInsertedInfo(
    val statements: List<XapiStatementEntity>,
)

/**
 * Insert statements that are used for report tests.
 */
suspend fun RespectSchoolDatabase.insertStatementsPerDay(
    numStatementsPerDay: Int = DEFAULT_NUM_STATEMENTS_PER_DAY,
    durationPerStatement: Long? = DEFAULT_DURATION_PER_STATEMENT,
    numDays: Int = DEFAULT_NUM_DAYS,
    statementClazzUid: (index: Int) -> Long = { DEFAULT_STATEMENT_CLAZZ_UID },
    resultDuration: Long? = durationPerStatement
): StatementsInsertedInfo {
    val today = Clock.System.now().toLocalDateTime(TimeZone.UTC)

    val statementList = (0 until numDays).flatMap { dayIndex ->
        //Adding 24 hours does not always get to the same time next day e.g. when daylight
        // savings time changes. Use LocalDateTime to workaround this.
        val timestamp = LocalDateTime(
            today.date.minus(DatePeriod(days = dayIndex)), today.time
        ).toInstant(TimeZone.UTC)

        (1..numStatementsPerDay).map { statementNum ->
            val statementUid = UUID.randomUUID()
            XapiStatementEntity(
                statementIdHi = statementUid.mostSignificantBits,
                statementIdLo = statementUid.leastSignificantBits,
                statementVerbUid = 1L,
                statementVerbId = "http://adlnet.gov/expapi/verbs/experienced",
                statementObjectType = XapiStatementEntityObjectTypeEnum.ACTIVITY,
                statementObjectActivityId = "http://example.com/activity",
                statementActorUid = 1L,
                resultDuration = resultDuration,
                resultSuccess = true,
                resultScoreScaled = 0.8f,
                timestamp = timestamp,
                stored = timestamp,
                statementObjectUid1 = statementClazzUid(statementNum)
            )
        }
    }

    getStatementDao().insertOrIgnoreListAsync(statementList)
    return StatementsInsertedInfo(statementList)
}
