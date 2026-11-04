package org.openeel.lib.xapi.extensions.reportoptions

enum class DefaultIndicator(
    val type: String,
    val sql: String,
) {
    TOTAL_CONTENT_USAGE_DURATION(
        type = YAxisTypes.DURATION.name,
        sql = "SUM(ResultSource.resultDuration)"
    ),
    AVERAGE_CONTENT_USAGE_DURATION(
        type = YAxisTypes.DURATION.name,
        sql = "SUM(ResultSource.resultDuration) / COUNT(DISTINCT ResultSource.contextRegistrationHash)"
    ),
    SCORE_AVERAGE(
        type = YAxisTypes.COUNT.name,
        sql = "AVG(ResultSource.resultScoreScaled)"
    ),
    SCORE_TOTAL(
        type = YAxisTypes.COUNT.name,
        sql = "SUM(ResultSource.resultScoreScaled)"
    ),
    NUMBER_OF_UNIQUE_USERS(
        type = YAxisTypes.COUNT.name,
        sql = "COUNT(DISTINCT CASE WHEN (SELECT actorObjectType FROM XapiActorEntity WHERE actorUid = ResultSource.statementActorUid) = 1 THEN ResultSource.statementActorUid END)"
    ),
}