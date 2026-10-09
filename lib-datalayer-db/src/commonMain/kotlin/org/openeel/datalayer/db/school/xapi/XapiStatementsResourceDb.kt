package world.respect.datalayer.db.school.xapi

import androidx.room.Transactor
import androidx.room.useReaderConnection
import androidx.room.useWriterConnection
import io.github.aakira.napier.Napier
import io.ktor.client.utils.buildHeaders
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.GetAuthenticatedPersonUseCase
import world.respect.datalayer.db.school.ext.isAdminOrTeacher
import world.respect.datalayer.db.school.ext.toLongPair
import world.respect.datalayer.db.school.xapi.adapters.ActorEntities
import world.respect.datalayer.db.school.xapi.adapters.StatementEntities
import world.respect.datalayer.db.school.xapi.adapters.VerbEntities
import world.respect.datalayer.db.school.xapi.adapters.identifierHash
import world.respect.datalayer.db.school.xapi.adapters.toEntities
import world.respect.datalayer.db.school.xapi.adapters.toModel
import world.respect.datalayer.db.school.xapi.adapters.toVerbEntities
import world.respect.datalayer.db.school.xapi.adapters.toXapiAssignmentResult
import world.respect.datalayer.db.school.xapi.composites.XapiStatementAndJsonEntities
import world.respect.datalayer.db.school.xapi.daos.XapiStatementEntityDao
import world.respect.lib.xapi.resources.local.XapiActivitiesResourceLocal
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.lib.xapi.resources.local.XapiStatementsResourceLocal
import world.respect.datalayer.school.xapi.ext.allDefinedActivities
import world.respect.lib.xapi.model.XapiStatement
import kotlin.time.Clock
import kotlin.uuid.Uuid
import world.respect.datalayer.db.school.xapi.entities.XapiStatementEntityObjectTypeEnum
import world.respect.datalayer.db.school.xapi.ext.allActivityUids
import world.respect.datalayer.db.school.xapi.ext.allActorUids
import world.respect.datalayer.ext.appendIfNotNull
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.resources.local.XapiAgentsResourceLocal
import world.respect.datalayer.school.xapi.ext.allActivities
import world.respect.datalayer.school.xapi.ext.allActors
import world.respect.datalayer.school.xapi.ext.allDefinedVerbs
import world.respect.datalayer.school.xapi.ext.allVerbs
import world.respect.lib.xapi.ext.distinctMerged
import world.respect.datalayer.school.xapi.ext.copyWithIdIfNotSet
import world.respect.datalayer.school.xapi.ext.distinctMerged
import world.respect.lib.dataloadstate.DataErrorResult
import world.respect.lib.dataloadstate.DataLoadMetaInfo
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.lib.xapi.OpenEelXapiConstants
import world.respect.lib.xapi.OpenEelXapiConstants.ACTIVITY_EXTENSION_WEBPUB_MANIFEST_LINK
import world.respect.lib.xapi.OpenEelXapiConstants.HEADER_XAPI_CONSISTENT_THROUGH
import world.respect.lib.xapi.OpenEelXapiConstants.HEADER_XAPI_VERSION
import world.respect.lib.xapi.composites.AssignmentAndProgress
import world.respect.lib.xapi.composites.XapiActorAndAssignmentProgress
import world.respect.lib.xapi.composites.XapiAssignmentTaskProgress
import world.respect.lib.xapi.exceptions.XapiException
import world.respect.lib.xapi.ext.distinctByMostRecentTimestampForActivityId
import world.respect.lib.xapi.ext.lastModifiedGMTStringForRetrievedStatements
import world.respect.lib.xapi.ext.mostRecentByTimestampOrNull
import world.respect.lib.xapi.ext.objectSubstatementOrNull
import world.respect.lib.xapi.ext.xapiRequireValidIRI
import world.respect.lib.xapi.model.AssignmentSummary
import world.respect.lib.xapi.model.XapiAccount
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiGroup
import world.respect.lib.xapi.model.XapiStatementRef
import world.respect.lib.xapi.model.XapiStatementResult
import world.respect.lib.xapi.model.XapiStatementTransformingSerializer
import world.respect.lib.xapi.model.XapiVerb
import world.respect.lib.xapi.resources.XapiStatementsResource.GetStatementParams
import kotlin.time.Instant

class XapiStatementsResourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
    private val getAuthenticatedPersonUseCase: GetAuthenticatedPersonUseCase,
    private val schoolUrl: Url,
    private val uidNumberMapper: UidNumberMapper,
    private val json: Json,
    private val xapiActivitiesResourceLocal: XapiActivitiesResourceLocal,
    private val xapiAgentsResourceLocal: XapiAgentsResourceLocal,
    private val xapiVersion: String = XAPI_VERSION,
) : XapiStatementsResourceLocal {

    private val authenticatedAgent = XapiAgent(
        account = XapiAccount(
            homePage = schoolUrl.toString(),
            name = authenticatedUser.guid,
        )
    )


    suspend fun doUpsertStatement(
        stmt: XapiStatement
    ) {
        //The statement received via HTTP should NOT have a stored time. If it does, we MUST remove
        //it to ensure concurrency and consistency results work as expected. This is allowed here
        //only to enable testing of exact statement storage.
        val storedTime = stmt.stored ?: Clock.System.now()

        val stmtTimestamp = stmt.timestamp ?: storedTime

        val exactStatement = stmt.copy(
            stored = storedTime,
            timestamp = stmtTimestamp,
            id = stmt.id ?: Uuid.random(),
        )

        val statementEntity = exactStatement.toEntities(
            uidNumberMapper = uidNumberMapper,
            json = json,
            isSubStatement = false,
        )

        schoolDb.getStatementDao().insertOrIgnoreListAsync(statementEntity.statements)
        schoolDb.getStatementEntityJsonDao().insertOrIgnoreListAsync(
            statementEntity.statementEntityJson
        )
        schoolDb.getStatementContextActivityJoinDao().insertOrIgnoreListAsync(
            statementEntity.statementContextActivityJoins
        )

        val verbEntities = stmt.allDefinedVerbs().map {
            it.toVerbEntities(uidNumberMapper)
        }

        schoolDb.getVerbDao().insertOrIgnoreAsync(
            verbEntities.map { it.verbEntity }
        )

        schoolDb.getVerbLangMapEntryDao().upsertList(
            verbEntities.flatMap { it.verbLangMapEntries }
        )

        val activities = stmt.allDefinedActivities().distinctMerged()
        xapiActivitiesResourceLocal.updateLocal(activities, stmtTimestamp)
        xapiAgentsResourceLocal.updateLocal(
            actors = stmt.allActors().distinctMerged(),
            timestamp = stmtTimestamp,
        )
    }

    private suspend fun List<XapiStatementAndJsonEntities>.mapToCanonicalStatements(
        idOnly: Boolean
    ): List<XapiStatement> {
        return map { entity ->
            val substatementEntity = schoolDb.takeIf {
                entity.stmtEntity.statementObjectType == XapiStatementEntityObjectTypeEnum.SUBSTATEMENT
            }?.getStatementDao()?.getEntityForSubstatement(
                subStatementIdHi = entity.stmtEntity.statementObjectUid1,
                subStatementIdLo = entity.stmtEntity.statementObjectUid2,
            )

            //now get StatementContextActivityJoins
            val contextActivityJoins = schoolDb.getStatementContextActivityJoinDao()
                .findAllByStatementIds(
                    statementIdHi = entity.stmtEntity.statementIdHi,
                    statementIdLo = entity.stmtEntity.statementIdLo,
                    statementIdHi2 = substatementEntity?.stmtEntity?.statementIdHi ?: 0,
                    statementIdLo2 = substatementEntity?.stmtEntity?.statementIdLo ?: 0,
                )

            val verbLangMapEntries = schoolDb.getVerbLangMapEntryDao()
                .takeIf { !idOnly }
                ?.findByVerbUidPair(
                    uid1 = entity.verbEntity?.verbUid ?: 0,
                    uid2 = substatementEntity?.verbEntity?.verbUid ?: 0,
                ) ?: emptyList()

            val verbs = listOfNotNull(
                entity.verbEntity, substatementEntity?.verbEntity,
            ).map { verbEntity ->
                VerbEntities(
                    verbEntity = verbEntity,
                    verbLangMapEntries = verbLangMapEntries.filter {
                        it.vlmeVerbUid == verbEntity.verbUid
                    }
                ).toModel()
            }

            val allActorUids = entity.stmtEntity.allActorUids().appendIfNotNull(
                substatementEntity?.stmtEntity?.allActorUids()
            )

            val actorEntities = schoolDb.getActorDao().findByUidList(
                uids = allActorUids
            )

            val groupMemberJoins = schoolDb.getGroupMemberActorJoinDao().findByGroupActorUidList(
                uidList = allActorUids,
                excludeIdentifiedGroups = idOnly,
            )

            val actors = actorEntities.filter {
                //Filter out agents that are referenced by being group members
                it.actorUid in allActorUids
            }.distinctBy {
                it.actorUid
            }.map { actorEntity ->
                //Passing through all actor entitie is harmless because the ActorEntities.toModel
                //will use the group member joins to select the actor entity for an agent member of
                //the group
                ActorEntities(
                    actor = actorEntity,
                    groupMemberJoins = groupMemberJoins.filter {
                        it.gmajGroupActorUid == actorEntity.actorUid
                    },
                    groupMemberAgents = actorEntities,
                ).toModel(idOnlyFormat = idOnly)
            }

            val activities = schoolDb.getActivityEntityDao()
                .takeIf { !idOnly }
                ?.findByUidList(
                    entity.stmtEntity.allActivityUids(contextActivityJoins).appendIfNotNull(
                        substatementEntity?.stmtEntity?.allActivityUids(contextActivityJoins)
                    )
                )?.map { it.toModel(json) } ?: emptyList()

            StatementEntities(
                statements = listOfNotNull(
                    entity.stmtEntity, substatementEntity?.stmtEntity
                ),
                statementEntityJson = emptyList(),
                statementContextActivityJoins = contextActivityJoins,
            ).toModel(
                json = json,
                statementIdHi = entity.stmtEntity.statementIdHi,
                statementIdLo = entity.stmtEntity.statementIdLo,
                uidNumberMapper = uidNumberMapper,
                verbs = verbs,
                actors = actors,
                activities = activities
            )
        }
    }


    private fun makeXapiHeadersForStatementResult(
        statements: List<XapiStatement>,
        consistentThrough: Instant,
        ascendingOrder: Boolean,
    ) = buildHeaders {
        set(
            name = HttpHeaders.LastModified,
            value = statements.lastModifiedGMTStringForRetrievedStatements(
                ascendingOrder, consistentThrough
            )
        )

        set(HEADER_XAPI_VERSION, xapiVersion)
        set(
            name = HEADER_XAPI_CONSISTENT_THROUGH,
            value = GMTDate(consistentThrough.toEpochMilliseconds()).toString()
        )
    }

    override suspend fun post(list: List<XapiStatement>): DataLoadState<List<Uuid>> {
        return try {
            val statementsWithIdsSet = list.map {
                it.copyWithIdIfNotSet()
            }

            //Run basic validations
            statementsWithIdsSet.forEach { stmt ->
                if(stmt.`object` !is XapiActivity && stmt.`object`.objectType == null) {
                    throw XapiException(400, "When StatementObject is not Activity, objectType MUST be set")
                }

                stmt.allVerbs().forEach {
                    xapiRequireValidIRI(it.id, "Verb id must be a valid IRI")
                }

                stmt.allActivities().forEach { activity ->
                    xapiRequireValidIRI(activity.id, "Activity id must be a valid IRI")
                    activity.definition?.extensions?.keys?.forEach { extensionKey ->
                        xapiRequireValidIRI(extensionKey, "Extension key must be a valid IRI")
                    }
                }

                buildList {
                    add(stmt)
                    stmt.objectSubstatementOrNull()?.also { add(it) }
                }.forEach { stmtOrSubStmt ->
                    stmtOrSubStmt.context?.extensions?.keys?.forEach { extensionKey ->
                        xapiRequireValidIRI(extensionKey, "Extension key must be a valid IRI")
                    }

                    stmtOrSubStmt.result?.score?.scaled?.also {
                        if(it < -1 || it > 1)
                            throw XapiException(400, "Score scaled must be between -1 and 1")
                    }
                }
            }

            val authenticatedPerson = getAuthenticatedPersonUseCase()
                ?: throw XapiException(403, "Not authenticated")

            if(!authenticatedPerson.isAdminOrTeacher()) {
                statementsWithIdsSet.forEach {
                    //Check all statements are for the authenticated user's actor only
                    if(it.actor.account?.homePage != schoolUrl.toString()
                        || it.actor.account?.name != authenticatedUser.guid
                    ) {
                        throw XapiException(403, "User does not have permission to post statement as any other actor")
                    }
                }
            }

            schoolDb.useWriterConnection { con ->
                con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                    statementsWithIdsSet.forEach { statement ->
                        val stmtId = statement.id
                            ?: throw IllegalStateException("Could not happen as per line 257")
                        val (stmtIdHi, stmtIdLo) = stmtId.toLongPair()
                        if(
                            schoolDb.getStatementDao().getTimestampsByUuid(
                                statementIdHi = stmtIdHi,
                                statementIdLo = stmtIdLo,
                            ) != null
                        ) {
                            throw XapiException(409, message = "Statement $stmtId already exists")
                        }

                        //check if this is a voiding statement
                        if(statement.verb.id == XapiVerb.ID_VOIDED) {
                            //find the statement we are going to void
                            val statementRef = statement.`object` as? XapiStatementRef
                                ?: throw XapiException(400, "Voiding statement object not statementref")
                            val (voidIdHi, voidIdLo) = Uuid.parse(statementRef.id).toLongPair()

                            val verbUidToVoid = schoolDb.getStatementDao().getVerbUidNumToBeVoided(
                                statementIdHi = voidIdHi,
                                statementIdLo = voidIdLo,
                            ) ?: throw XapiException(403, "Statement to void not found")

                            if(uidNumberMapper(XapiVerb.ID_VOIDED) == verbUidToVoid)
                                throw XapiException(403, "Cannot void a void statement")

                            schoolDb.getStatementDao().updateSetStatementVoided(
                                voidIdHi, voidIdLo
                            )
                        }

                        doUpsertStatement(statement)
                    }
                }
            }

            DataReadyState(
                data = statementsWithIdsSet.mapNotNull { it.id }
            )
        }catch(e: Throwable) {
            DataErrorResult(error = e)
        }
    }

    override suspend fun updateLocal(
        list: List<XapiStatement>,
    ) {
        //needs to check for existing statement, if existing, do nothing.
        schoolDb.useWriterConnection { con ->
            val storedTime = Clock.System.now()

            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                list.map {
                    it.copy(stored = storedTime)
                }.forEach { statement ->
                    val (stmtIdHi, stmtIdLo) = (statement.id?.toLongPair()
                        ?: throw IllegalArgumentException("statement to store must have ids"))
                    val timesInDb = schoolDb.getStatementDao().getTimestampsByUuid(
                        statementIdHi = stmtIdHi,
                        statementIdLo = stmtIdLo,
                    )

                    //Because statements are immutable, if it is already in the db, do NOTHING.
                    if(timesInDb == null) {
                        doUpsertStatement(statement)
                    }
                }
            }
        }
    }

    override suspend fun get(
        listParams: GetStatementParams,
        dataLoadParams: DataLoadParams,
    ): DataLoadState<XapiStatementResult> {

        val statementIds = listParams.statementId?.toLongPair()
        val voidedStatementIds = listParams.voidedStatementId?.toLongPair()
        val format = listParams.format ?: XapiStatementsResource.GetStatementFormatEnum.EXACT
        val ascendingOrder = listParams.ascending
        val consistentThrough = Clock.System.now()

        return schoolDb.useReaderConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.DEFERRED) {
                val statements =  if(format == XapiStatementsResource.GetStatementFormatEnum.EXACT) {
                    schoolDb.getStatementEntityJsonDao().list(
                        statementIdHi = statementIds?.first ?: 0,
                        statementIdLo = statementIds?.second ?: 0,
                        voidedStatementIdHi = voidedStatementIds?.first ?: 0,
                        voidedStatementIdLo = voidedStatementIds?.second ?: 0,
                        agentUid = listParams.agent?.identifierHash(uidNumberMapper) ?: 0,
                        verbUid = listParams.verb?.let { uidNumberMapper(it) } ?: 0,
                        activityUid = listParams.activity?.let { uidNumberMapper(it) } ?: 0,
                        relatedAgents = listParams.relatedAgents,
                        relatedActivities = listParams.relatedActivities,
                        since = listParams.since?.toEpochMilliseconds() ?: XapiStatementEntityDao.SINCE_UNSET,
                        until = listParams.until?.toEpochMilliseconds() ?: XapiStatementEntityDao.UNTIL_UNSET,
                        ascending = ascendingOrder,
                        limit = listParams.limit ?: DEFAULT_MAX_STATEMENTS,
                        authenticatedPersonUidNum = uidNumberMapper(authenticatedUser.guid),
                        authenticatedActorUid = authenticatedAgent.identifierHash(uidNumberMapper),
                        appListingVerbUid = uidNumberMapper(XapiVerb.ID_LISTED_APP),
                    ).map { entity ->
                        json.decodeFromString(
                            XapiStatementTransformingSerializer, entity.fullStatement
                        )
                    }
                }else {
                    schoolDb.getStatementDao().list(
                        statementIdHi = statementIds?.first ?: 0,
                        statementIdLo = statementIds?.second ?: 0,
                        voidedStatementIdHi = voidedStatementIds?.first ?: 0,
                        voidedStatementIdLo = voidedStatementIds?.second ?: 0,
                        agentUid = listParams.agent?.identifierHash(uidNumberMapper) ?: 0,
                        verbUid = listParams.verb?.let { uidNumberMapper(it) } ?: 0,
                        activityUid = listParams.activity?.let { uidNumberMapper(it) } ?: 0,
                        relatedAgents = listParams.relatedAgents,
                        relatedActivities = listParams.relatedActivities,
                        since = listParams.since?.toEpochMilliseconds() ?: XapiStatementEntityDao.SINCE_UNSET,
                        until = listParams.until?.toEpochMilliseconds() ?: XapiStatementEntityDao.UNTIL_UNSET,
                        ascending = ascendingOrder,
                        limit = listParams.limit ?: DEFAULT_MAX_STATEMENTS,
                        authenticatedPersonUidNum = uidNumberMapper(authenticatedUser.guid),
                        authenticatedActorUid = authenticatedAgent.identifierHash(uidNumberMapper),
                        appListingVerbUid = uidNumberMapper(XapiVerb.ID_LISTED_APP),
                    ).mapToCanonicalStatements(
                        idOnly = format == XapiStatementsResource.GetStatementFormatEnum.IDS
                    )
                }

                DataReadyState(
                    data = XapiStatementResult(
                        statements = statements,
                        more = null,
                    ),
                    metaInfo = DataLoadMetaInfo(
                        headers = makeXapiHeadersForStatementResult(
                            statements, consistentThrough, ascendingOrder
                        )
                    )
                )
            }
        }
    }

    override fun getAsFlow(
        listParams: GetStatementParams,
        dataLoadParams: DataLoadParams
    ): Flow<DataLoadState<XapiStatementResult>> {
        val statementIds = listParams.statementId?.toLongPair()
        val voidedStatementIds = listParams.voidedStatementId?.toLongPair()
        val format = listParams.format ?: XapiStatementsResource.GetStatementFormatEnum.EXACT

        return if(format == XapiStatementsResource.GetStatementFormatEnum.EXACT) {
            schoolDb.getStatementEntityJsonDao().listAsFlow(
                statementIdHi = statementIds?.first ?: 0,
                statementIdLo = statementIds?.second ?: 0,
                voidedStatementIdHi = voidedStatementIds?.first ?: 0,
                voidedStatementIdLo = voidedStatementIds?.second ?: 0,
                agentUid = listParams.agent?.identifierHash(uidNumberMapper) ?: 0,
                verbUid = listParams.verb?.let { uidNumberMapper(it) } ?: 0,
                activityUid = listParams.activity?.let { uidNumberMapper(it) } ?: 0,
                relatedAgents = listParams.relatedAgents,
                relatedActivities = listParams.relatedActivities,
                since = listParams.since?.toEpochMilliseconds() ?: XapiStatementEntityDao.SINCE_UNSET,
                until = listParams.until?.toEpochMilliseconds() ?: XapiStatementEntityDao.UNTIL_UNSET,
                ascending = listParams.ascending,
                limit = listParams.limit ?: DEFAULT_MAX_STATEMENTS,
                authenticatedPersonUidNum = uidNumberMapper(authenticatedUser.guid),
                authenticatedActorUid = authenticatedAgent.identifierHash(uidNumberMapper),
                appListingVerbUid = uidNumberMapper(XapiVerb.ID_LISTED_APP),
            ).map { list ->
                DataReadyState(
                    data = XapiStatementResult(
                        statements = list.map { entity ->
                            json.decodeFromString(
                                XapiStatementTransformingSerializer, entity.fullStatement
                            )
                        },
                        more = null
                    )
                )
            }
        }else {
            schoolDb.getStatementDao().listAsFlow(
                statementIdHi = statementIds?.first ?: 0,
                statementIdLo = statementIds?.second ?: 0,
                voidedStatementIdHi = voidedStatementIds?.first ?: 0,
                voidedStatementIdLo = voidedStatementIds?.second ?: 0,
                agentUid = listParams.agent?.identifierHash(uidNumberMapper) ?: 0,
                verbUid = listParams.verb?.let { uidNumberMapper(it) } ?: 0,
                activityUid = listParams.activity?.let { uidNumberMapper(it) } ?: 0,
                relatedAgents = listParams.relatedAgents,
                relatedActivities = listParams.relatedActivities,
                since = listParams.since?.toEpochMilliseconds() ?: XapiStatementEntityDao.SINCE_UNSET,
                until = listParams.until?.toEpochMilliseconds() ?: XapiStatementEntityDao.UNTIL_UNSET,
                ascending = listParams.ascending,
                limit = listParams.limit ?: DEFAULT_MAX_STATEMENTS,
                authenticatedPersonUidNum = uidNumberMapper(authenticatedUser.guid),
                authenticatedActorUid = authenticatedAgent.identifierHash(uidNumberMapper),
                appListingVerbUid = uidNumberMapper(XapiVerb.ID_LISTED_APP),
            ).map { list ->
                val consistentThrough = Clock.System.now()

                val statements = list.mapToCanonicalStatements(
                    idOnly = format == XapiStatementsResource.GetStatementFormatEnum.IDS
                )
                DataReadyState(
                    data = XapiStatementResult(
                        statements = statements,
                        more = null,
                    ),
                    metaInfo = DataLoadMetaInfo(
                        headers = makeXapiHeadersForStatementResult(
                            statements, consistentThrough, listParams.ascending,
                        )
                    )
                )
            }
        }
    }

    override suspend fun getByUuid(uuid: Uuid): XapiStatement? {
        return get(
            listParams = GetStatementParams(statementId = uuid)
        ).dataOrNull()?.statements?.firstOrNull()
    }

    override fun getAssignmentProgress(
        activityId: String,
        filterByAssigneeAgent: XapiAgent?,
    ): Flow<DataLoadState<AssignmentAndProgress>> {
        //Get the statement itself to get the actor and list of assigned activities.
        return getAsFlow(
            listParams = GetStatementParams(
                activity = activityId,
                relatedActivities = false,
                format = XapiStatementsResource.GetStatementFormatEnum.EXACT,
            ),
            dataLoadParams = DataLoadParams(),
        ).map { statementResult ->
            schoolDb.useReaderConnection { con ->
                con.withTransaction(Transactor.SQLiteTransactionType.DEFERRED) {
                    val exactAssignmentStmt = statementResult.dataOrNull()?.statements
                        ?.mostRecentByTimestampOrNull()

                    val canonicalAssignmentStmt = exactAssignmentStmt?.let {
                        get(
                            listParams = GetStatementParams(
                                statementId = exactAssignmentStmt.id,
                                limit = 1,
                                format = XapiStatementsResource.GetStatementFormatEnum.CANONICAL,
                            )
                        )
                    }?.dataOrNull()?.statements?.firstOrNull()

                    if(exactAssignmentStmt == null || canonicalAssignmentStmt == null) {
                        NoDataLoadedState(reason = NoDataLoadedState.Reason.NOT_FOUND)
                    }else {
                        /*
                         * The canonical statement will include the latest definition of the assignee (actor).
                         * However we need the exact statement to
                         *  a) Get the task activities in the correct order
                         *  b) Be sure that the definition of each task activity includes the web pub manifest
                         *     url extension (the canonical definition of the activity might change, e.g. if
                         *     statements from the content itself don't include the web pub manifest).
                         */
                        val assignmentStmt = exactAssignmentStmt.copy(actor = canonicalAssignmentStmt.actor)

                        val assignedActivities = exactAssignmentStmt.context?.contextActivities?.grouping
                            ?: emptyList()

                        val statementActor = assignmentStmt.actor
                        val actorsToShow : List<XapiAgent> = when {
                            filterByAssigneeAgent != null -> listOfNotNull(filterByAssigneeAgent)

                            statementActor is XapiAgent -> listOf(statementActor)

                            //Might not have loaded yet.
                            statementActor is XapiGroup -> statementActor.member ?: emptyList()

                            else -> {
                                throw IllegalStateException("statement actor must be agent or group")
                            }
                        }

                        val dbAssignmentResults = schoolDb.getStatementDao().getAssignmentResults(
                            assignmentActivityIdNum = uidNumberMapper(activityId),
                            filterByStudentActorUid = filterByAssigneeAgent
                                ?.identifierHash(uidNumberMapper) ?: 0L,
                            completeVerbUid = uidNumberMapper(XapiVerb.ID_COMPLETED),
                        ).groupBy { it.actorUid }

                        DataReadyState(
                            data = AssignmentAndProgress(
                                assignmentStatement = assignmentStmt,
                                progress = actorsToShow.map { actor ->
                                    val actorUidNum = actor.identifierHash(uidNumberMapper)
                                    XapiActorAndAssignmentProgress(
                                        actor = actor,
                                        progressPerTask = assignedActivities.map { taskActivity ->
                                            dbAssignmentResults[actorUidNum]?.firstOrNull {
                                                it.activityUid == uidNumberMapper(taskActivity.id)
                                            }?.toXapiAssignmentResult(taskActivity.id)
                                                ?: XapiAssignmentTaskProgress.emptyResult(taskActivity.id)
                                        }
                                    )
                                }
                            )
                        )
                    }
                }
            }
        }
    }

    override fun getAssignmentListAsFlow(
        dataLoadParams: DataLoadParams,
        studentAgent: XapiAgent?
    ): Flow<DataLoadState<List<AssignmentSummary>>> {
        val flowIn = if(studentAgent != null) {
            schoolDb.getStatementDao().getAssignmentListForStudentAsFlow(
                assignVerbUid = uidNumberMapper(XapiVerb.ID_ASSIGN),
                studentAgentActorUid = studentAgent.identifierHash(uidNumberMapper),
                completeVerbUid = uidNumberMapper(XapiVerb.ID_COMPLETED),
                deadlineExtensionHash = uidNumberMapper(OpenEelXapiConstants.ACTIVITY_EXTENSION_DEADLINE),
            )
        }else {
            schoolDb.getStatementDao().getAssignmentListAsFlow(
                assignmentRecipeActivityUid = uidNumberMapper(
                    OpenEelXapiConstants.CATEGORY_ASSIGNMENT_RECIPE
                ),
                assignVerbUid = uidNumberMapper(XapiVerb.ID_ASSIGN),
                completedVerbUid = uidNumberMapper(XapiVerb.ID_COMPLETED),
                deadlineExtensionHash = uidNumberMapper(OpenEelXapiConstants.ACTIVITY_EXTENSION_DEADLINE),
            )
        }

        return flowIn.map { list ->
            DataReadyState(
                data = list.map { summaryRow ->
                    val assignmentStmt = get(
                        listParams = GetStatementParams(
                            activity = summaryRow.activityId,
                            format = XapiStatementsResource.GetStatementFormatEnum.EXACT,
                        )
                    ).dataOrNull()?.statements?.distinctByMostRecentTimestampForActivityId()
                        ?.sortedByDescending { it.timestamp }
                        ?.firstOrNull()

                    val manifestUrls = assignmentStmt?.context?.contextActivities?.grouping
                        ?.mapNotNull {
                            it.definition?.extensions?.get(ACTIVITY_EXTENSION_WEBPUB_MANIFEST_LINK)
                                ?.jsonPrimitive?.contentOrNull
                                ?.let {
                                    try {
                                        Url(it)
                                    }catch(e: Throwable) {
                                        Napier.w("Exception getting manifest url", e)
                                        null
                                    }
                                }
                        } ?: emptyList()

                    val deadline = try {
                        val jsonPrimitive = summaryRow.deadlineStr?.let {
                            json.decodeFromString(JsonPrimitive.serializer(), it)
                        }

                        jsonPrimitive?.contentOrNull?.let { Instant.parse(it) }
                    }catch (e: Throwable) {
                        Napier.w("Exception decoding deadline string", e)
                        null
                    }
                    AssignmentSummary(
                        activityId = summaryRow.activityId,
                        title = summaryRow.title ?: "",
                        assignedActor = ActorEntities(
                            actor = summaryRow.actorEntity,
                        ).toModel(idOnlyFormat = true),
                        lastModified = Clock.System.now(),
                        deadline = deadline,
                        completedCount = summaryRow.numCompleted,
                        totalCount = summaryRow.numTotal,
                        averageScore = summaryRow.averageScoreScaled,
                        learningUnitManifestUrls = manifestUrls,
                    )
                }
            )
        }
    }
    companion object {
        const val DEFAULT_MAX_STATEMENTS = 5_000

        const val XAPI_VERSION = "1.0.3"

    }

}
