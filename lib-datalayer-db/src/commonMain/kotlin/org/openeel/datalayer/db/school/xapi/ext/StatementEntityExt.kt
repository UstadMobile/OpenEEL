package world.respect.datalayer.db.school.xapi.ext

import world.respect.datalayer.db.school.xapi.entities.XapiStatementContextActivityJoin
import world.respect.datalayer.db.school.xapi.entities.XapiStatementEntityObjectTypeEnum
import world.respect.datalayer.db.school.xapi.entities.XapiStatementEntity

internal val XapiStatementEntity.hasResult: Boolean
    get() = resultCompletion != null ||
            resultSuccess != null ||
            resultResponse != null ||
            resultDuration != null ||
            resultExtensions != null ||
            hasResultScore

internal val XapiStatementEntity.hasResultScore: Boolean
    get() = resultScoreScaled != null ||
            resultScoreRaw != null  ||
            resultScoreMin != null ||
            resultScoreMax != null

internal val XapiStatementEntity.hasContext: Boolean
    get() = contextRegistrationHi != 0L ||
            contextRegistrationLo != 0L ||
            contextInstructorActorUid != 0L


/**
 * Get the UIDS of all actors associated with this statemententity. This does not
 * include any substatement.
 */
fun XapiStatementEntity.allActorUids(): List<Long> {
    return listOfNotNull(
        statementActorUid,
        statementObjectUid1.takeIf {
            statementObjectType == XapiStatementEntityObjectTypeEnum.AGENT ||
                    statementObjectType == XapiStatementEntityObjectTypeEnum.GROUP
        },
        contextTeamActorUid.takeIf { it != 0L },
        contextInstructorActorUid.takeIf { it != 0L },
        authorityActorUid.takeIf { it != 0L },
    )
}

/**
 * Get the UIDS of all Activities associated with this statemententity. This does not
 * include the substatement, if any.
 */
fun XapiStatementEntity.allActivityUids(
    joins: List<XapiStatementContextActivityJoin>
): List<Long> {
    return listOfNotNull(
        statementObjectUid1.takeIf {
            statementObjectType == XapiStatementEntityObjectTypeEnum.ACTIVITY
        }
    ) + joins.map { it.scajToActivityUid }
}

