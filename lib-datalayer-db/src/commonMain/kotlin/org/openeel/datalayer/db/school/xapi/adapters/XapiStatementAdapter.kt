package world.respect.datalayer.db.school.xapi.adapters

import kotlinx.serialization.json.Json
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.ext.toLongPair
import world.respect.datalayer.db.school.xapi.entities.XapiStatementContextActivityJoin
import world.respect.datalayer.db.school.xapi.entities.XapiStatementContextActivityJoinTypeEnum
import world.respect.datalayer.db.school.xapi.entities.XapiStatementEntity
import world.respect.datalayer.db.school.xapi.entities.XapiStatementEntityJson
import world.respect.datalayer.db.school.xapi.entities.XapiStatementEntityObjectTypeEnum
import world.respect.datalayer.db.school.xapi.ext.hasContext
import world.respect.datalayer.db.school.xapi.ext.hasResult
import world.respect.datalayer.db.school.xapi.ext.hasResultScore
import world.respect.datalayer.db.school.xapi.ext.uuidForSubstatement
import world.respect.datalayer.db.school.xapi.xapiExtensionsSerializer
import world.respect.datalayer.db.shared.ext.takeIfNotEmpty
import world.respect.datalayer.school.xapi.ext.isCompletionOrProgress
import world.respect.datalayer.school.xapi.ext.resultProgressExtension
import world.respect.lib.xapi.exceptions.XapiException
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiActor
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiContext
import world.respect.lib.xapi.model.XapiContextActivities
import world.respect.lib.xapi.model.XapiGroup
import world.respect.lib.xapi.model.XapiObjectType
import world.respect.lib.xapi.model.XapiResult
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiStatementObject
import world.respect.lib.xapi.model.XapiStatementRef
import world.respect.lib.xapi.model.XapiStatementTransformingSerializer
import world.respect.lib.xapi.model.XapiVerb
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * @property statements a list because even a single statement can require multiple StatementEntity(s)
 *           when it includes a sub statement.
 */
data class StatementEntities(
    val statements: List<XapiStatementEntity> = emptyList(),
    val statementEntityJson: List<XapiStatementEntityJson> = emptyList(),
    val statementContextActivityJoins: List<XapiStatementContextActivityJoin> = emptyList(),
)


/**
 * As per the spec, if the objectType is not specified, it defaults to Activity.
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#2441-when-the-objecttype-is-activity
 */
val XapiStatementObject.objectTypeEnum: XapiStatementEntityObjectTypeEnum
    get() = objectType?.entityObjectTypeEnum ?: XapiStatementEntityObjectTypeEnum.ACTIVITY

@OptIn(ExperimentalUuidApi::class)
fun XapiStatementObject.objectForeignKeys(
    uidNumberMapper: UidNumberMapper,
    statementUuid: Uuid,
): Pair<Long, Long> {
    return when(this) {
        is XapiActivity -> {
            Pair(uidNumberMapper(id), 0)
        }
        is XapiAgent -> {
            Pair(this.identifierHash(uidNumberMapper), 0)
        }
        is XapiGroup -> {
            Pair(this.identifierHash(uidNumberMapper), 0)
        }
        is XapiStatementRef -> {
            Uuid.parse(id).toLongs { mostSignificantBits, leastSignificantBits ->
                Pair(mostSignificantBits, leastSignificantBits)
            }
        }
        is XapiStatement -> {
            //As per the doc on StatementEntity itself, where there is a substatement, the
            //statement uid is the uid of the statement itself + 1.
            statementUuid.uuidForSubstatement().toLongPair()
        }
    }
}

/**
 * Convert xAPI Statement JSON instead entities that can be stored in the database.
 *
 * Most of the time the statement received will be when running an xAPI activity, and the actor will
 * be an Agent for the current user.
 *
 * @param isSubStatement true if the statement being processed is a substatement, in which case, it
 * must not as per the spec contain another nested substatement.
 *
 * @return list of two statement entities - the statement itself, and the entities of the object (
 * which could be a substatement, agent, group, or statementref)
 */
@OptIn(ExperimentalUuidApi::class)
fun XapiStatement.toEntities(
    uidNumberMapper: UidNumberMapper,
    json: Json,
    isSubStatement: Boolean = false,
): StatementEntities {
    val statementUuid = id ?: throw IllegalArgumentException("Statement must have id set before conversion to entities")
    val (stmtUuidHi, stmtUuidLo) = statementUuid.toLongPair()

    fun MutableList<XapiStatementContextActivityJoin>.addStatementContextJoins(
        activities: List<XapiActivity>?,
        typeEnum: XapiStatementContextActivityJoinTypeEnum,
    ) {
        activities?.also { activityList ->
            addAll(
                activityList.map {
                    it.toContextActivityJoinEntity(
                        type = typeEnum,
                        uidNumberMapper = uidNumberMapper,
                        statementUuidHi = stmtUuidHi,
                        statementUuidLo = stmtUuidLo,
                    )
                }
            )
        }
    }

    if(isSubStatement && `object` is XapiStatement)
        throw XapiException(
            400, "SubStatement cannot have another nested subs== XapiObjectType.SubStatementtatement"
        )

    val contextRegistration = context?.registration

    val statementObjectForeignKeys = `object`.objectForeignKeys(uidNumberMapper, statementUuid)

    val contextRegHiLo = contextRegistration?.toLongPair()

    val substatementEntities = (`object` as? XapiStatement)?.copy(
       id = statementUuid.uuidForSubstatement(),
    )?.toEntities(
        uidNumberMapper = uidNumberMapper,
        json = json,
        isSubStatement = true,
    )

    return StatementEntities(
        statements = listOf(
            XapiStatementEntity(
                statementIdHi = stmtUuidHi,
                statementIdLo = stmtUuidLo,
                statementActorUid = this.actor.identifierHash(uidNumberMapper),
                authorityActorUid = this.authority?.identifierHash(uidNumberMapper) ?: 0,
                statementVerbUid = uidNumberMapper(verb.id),
                statementVerbId = verb.id,
                resultCompletion = result?.completion,
                resultSuccess = result?.success,
                resultScoreScaled = result?.score?.scaled,
                resultScoreRaw = result?.score?.raw,
                resultScoreMin = result?.score?.min,
                resultScoreMax = result?.score?.max,
                resultDuration = result?.duration?.inWholeMilliseconds,
                resultResponse = result?.response,
                resultExtensions = result?.extensions?.let {
                    json.encodeToString(xapiExtensionsSerializer, it)
                },
                timestamp = timestamp,
                stored = stored,
                contextRegistrationHi = contextRegHiLo?.first ?: 0,
                contextRegistrationLo = contextRegHiLo?.second ?: 0,
                contextPlatform = context?.platform,
                contextLanguage = context?.language,
                contextRevision = context?.revision,
                contextTeamActorUid = context?.team?.identifierHash(uidNumberMapper) ?: 0,
                contextInstructorActorUid = context?.instructor?.identifierHash(uidNumberMapper) ?: 0,
                completionOrProgress = isCompletionOrProgress(),
                extensionProgress = resultProgressExtension,
                statementObjectType = `object`.objectTypeEnum,
                statementObjectUid1 = statementObjectForeignKeys.first,
                statementObjectUid2 = statementObjectForeignKeys.second,
                statementObjectActivityId = (`object` as? XapiActivity)?.id,
                isSubStatement = isSubStatement,
                statementVersion = version,
            )
        ) + (substatementEntities?.statements ?: emptyList()),
        statementEntityJson = if(!isSubStatement) {
            listOf(
                XapiStatementEntityJson(
                    stmtJsonIdHi = stmtUuidHi,
                    stmtJsonIdLo = stmtUuidLo,
                    fullStatement = json.encodeToString(
                        serializer = XapiStatementTransformingSerializer, this
                    ),
                )
            )
        }else {
            emptyList()
        },
        statementContextActivityJoins = buildList {
            context?.contextActivities?.also { contextActivities ->
                addStatementContextJoins(
                    contextActivities.parent, XapiStatementContextActivityJoinTypeEnum.PARENT
                )
                addStatementContextJoins(
                    contextActivities.grouping, XapiStatementContextActivityJoinTypeEnum.GROUPING
                )
                addStatementContextJoins(
                    contextActivities.category, XapiStatementContextActivityJoinTypeEnum.CATEGORY
                )
                addStatementContextJoins(
                    contextActivities.other, XapiStatementContextActivityJoinTypeEnum.OTHER
                )
            }
        } + (substatementEntities?.statementContextActivityJoins ?: emptyList())
    )
}

fun StatementEntities.toModel(
    json: Json,
    statementIdHi: Long,
    statementIdLo: Long,
    uidNumberMapper: UidNumberMapper,
    actors: List<XapiActor>,
    activities: List<XapiActivity>,
    verbs: List<XapiVerb>,
) : XapiStatement {
    val primaryStatementEntity = statements.first {
        it.statementIdHi == statementIdHi && it.statementIdLo == statementIdLo
    }

    val actorMap = actors.associateBy { it.identifierHash(uidNumberMapper) }
    val verbMap = verbs.associateBy { uidNumberMapper(it.id) }
    val activityMap = activities.associateBy { uidNumberMapper(it.id) }

    fun List<XapiStatementContextActivityJoin>.filterToModel(
        type: XapiStatementContextActivityJoinTypeEnum
    ) : List<XapiActivity>? {
        return filter {
            //Check statement id to ensure it is not the substatement
            it.scajFromStatementIdHi == statementIdHi &&
            it.scajFromStatementIdLo == statementIdLo &&
            it.scajContextType == type
        }.map {
            activityMap[it.scajToActivityUid] ?: XapiActivity(
                objectType = XapiObjectType.Activity,
                id = it.scajToActivityId,
            )
        }.takeIfNotEmpty()
    }

    val statementUuid = Uuid.fromLongs(
        primaryStatementEntity.statementIdHi,
        primaryStatementEntity.statementIdLo
    )

    return XapiStatement(
        id = statementUuid.takeIf { !primaryStatementEntity.isSubStatement },
        actor = actorMap[primaryStatementEntity.statementActorUid] ?: throw IllegalStateException("no primary actor"),
        verb = verbMap[primaryStatementEntity.statementVerbUid] ?: XapiVerb(
            id = primaryStatementEntity.statementVerbId
        ),
        `object` = when(primaryStatementEntity.statementObjectType) {
            XapiStatementEntityObjectTypeEnum.ACTIVITY -> {
                XapiActivity(
                    objectType = XapiObjectType.Activity,
                    id = primaryStatementEntity.statementObjectActivityId
                        ?: throw IllegalStateException("Statement.toModel: object is an activity but statementObjectActivityId is null"),
                    definition = activityMap[primaryStatementEntity.statementObjectUid1]?.definition,
                )
            }

            XapiStatementEntityObjectTypeEnum.SUBSTATEMENT -> {
                val substatementUuidVals = statementUuid.uuidForSubstatement().toLongPair()

                this.toModel(
                    json = json,
                    uidNumberMapper = uidNumberMapper,
                    actors = actors,
                    activities = activities,
                    verbs = verbs,
                    statementIdHi = substatementUuidVals.first,
                    statementIdLo = substatementUuidVals.second,
                )
            }

            XapiStatementEntityObjectTypeEnum.STATEMENT_REF -> {
                XapiStatementRef(
                    id = Uuid.fromLongs(
                        primaryStatementEntity.statementObjectUid1,
                        primaryStatementEntity.statementObjectUid2,
                    ).toString()
                )
            }

            XapiStatementEntityObjectTypeEnum.AGENT -> {
                val actor = actorMap[primaryStatementEntity.statementObjectUid1]
                    ?: throw IllegalStateException("Object type is agent, but agent not in actor list")
                actor as XapiAgent
            }

            XapiStatementEntityObjectTypeEnum.GROUP -> {
                val actor = actorMap[primaryStatementEntity.statementObjectUid1]
                    ?: throw IllegalStateException("Object type is group, but group not in actor list")
                actor as XapiGroup
            }
        },
        result = if(primaryStatementEntity.hasResult) {
            XapiResult(
                completion = primaryStatementEntity.resultCompletion,
                success = primaryStatementEntity.resultSuccess,
                score = if(primaryStatementEntity.hasResultScore) {
                    XapiResult.Score(
                        scaled = primaryStatementEntity.resultScoreScaled,
                        raw = primaryStatementEntity.resultScoreRaw,
                        max = primaryStatementEntity.resultScoreMax,
                        min = primaryStatementEntity.resultScoreMin,
                    )
                }else {
                    null
                },
                duration = primaryStatementEntity.resultDuration?.milliseconds,
                response = primaryStatementEntity.resultResponse,
                extensions = primaryStatementEntity.resultExtensions?.let {
                    json.decodeFromString(
                        xapiExtensionsSerializer, it
                    )
                },
            )
        }else {
            null
        },
        context = if(primaryStatementEntity.hasContext || statementContextActivityJoins.isNotEmpty()) {
            val hasRegistration = primaryStatementEntity.contextRegistrationHi != 0L &&
                    primaryStatementEntity.contextRegistrationLo != 0L
            XapiContext(
                instructor = actorMap[primaryStatementEntity.contextInstructorActorUid],
                registration = if(hasRegistration) {
                    Uuid.fromLongs(
                        primaryStatementEntity.contextRegistrationHi,
                        primaryStatementEntity.contextRegistrationLo,
                    )
                }else {
                    null
                },
                language = primaryStatementEntity.contextLanguage,
                platform = primaryStatementEntity.contextPlatform,
                revision = primaryStatementEntity.contextRevision,
                team = actorMap[primaryStatementEntity.contextTeamActorUid],
                contextActivities = XapiContextActivities(
                    parent = statementContextActivityJoins.filterToModel(
                        XapiStatementContextActivityJoinTypeEnum.PARENT
                    ),
                    grouping = statementContextActivityJoins.filterToModel(
                        XapiStatementContextActivityJoinTypeEnum.GROUPING
                    ),
                    category = statementContextActivityJoins.filterToModel(
                        XapiStatementContextActivityJoinTypeEnum.CATEGORY
                    ),
                    other = statementContextActivityJoins.filterToModel(
                        XapiStatementContextActivityJoinTypeEnum.OTHER
                    ),
                ).takeIf {
                    it.parent != null || it.grouping != null || it.category != null || it.other != null
                }
            )
        }else {
            null
        },
        timestamp = primaryStatementEntity.timestamp,
        stored = primaryStatementEntity.stored,
        objectType = if(primaryStatementEntity.isSubStatement) {
            XapiObjectType.SubStatement
        }else {
            null
        },
        authority = actorMap[primaryStatementEntity.authorityActorUid],
        version = primaryStatementEntity.statementVersion,
    )
}
