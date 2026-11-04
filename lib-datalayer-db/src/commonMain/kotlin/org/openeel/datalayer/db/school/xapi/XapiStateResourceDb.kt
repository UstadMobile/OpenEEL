package org.openeel.datalayer.db.school.xapi

import androidx.room.Transactor
import androidx.room.useReaderConnection
import androidx.room.useWriterConnection
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.http.quote
import io.ktor.http.toHttpDate
import io.ktor.util.sha1
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import org.openeel.datalayer.db.RespectSchoolDatabase
import org.openeel.datalayer.db.school.xapi.adapters.toXapiStateDocumentEntity
import org.openeel.datalayer.db.school.xapi.entities.XapiStateDocumentShaEntity
import org.openeel.datalayer.db.shared.InstantAsTimestampString
import org.openeel.datalayer.db.shared.toModel
import org.openeel.lib.dataloadstate.DataLoadMetaInfo
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.lib.dataloadstate.ext.hasIfNotModifiedHeaders
import org.openeel.lib.dataloadstate.ext.isStillValid
import org.openeel.lib.dataloadstate.ext.requestEtagAndLastModified
import org.openeel.lib.xapi.auth.GetAuthenticatedXapiAgentsUseCase
import org.openeel.lib.xapi.exceptions.XapiException
import org.openeel.lib.xapi.ext.isJson
import org.openeel.lib.xapi.ext.mergeJsonDoc
import org.openeel.lib.xapi.ext.requireIfi
import org.openeel.lib.xapi.model.XapiAgent
import org.openeel.lib.xapi.model.XapiDocument
import org.openeel.lib.xapi.resources.XapiStateResource
import org.openeel.lib.xapi.resources.local.XapiStateResourceLocal
import kotlin.uuid.Uuid

class XapiStateResourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val json: Json,
    private val getAuthenticatedXapiAgentsUseCase: GetAuthenticatedXapiAgentsUseCase,
) : XapiStateResourceLocal {

    override suspend fun updateLocal(
        params: XapiStateResource.SingleDocumentParams,
        document: XapiDocument
    ) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val existing = schoolDb.getStateDocumentDao().findByActivityIriAndAgentIfiAndRegistrationAndStateId(
                    activityIri = params.activityId,
                    agentIfi = params.agent.requireIfi(),
                    registration = params.registration,
                    stateId = params.stateId,
                )
                val entity = document.toXapiStateDocumentEntity(
                    params = params,
                    id = existing?.document?.id,
                )

                schoolDb.getStateDocumentDao().upsert(entity)
                schoolDb.getStateDocumentShaDao().upsert(
                    XapiStateDocumentShaEntity(
                        docId = entity.id,
                        sha1digest = sha1(entity.contents).toHexString(),
                    )
                )
            }
        }
    }

    private suspend fun requireAuthenticatedPersonMatchesActor(
        actor: XapiAgent,
    ) {
        val authenticatedPerson = getAuthenticatedXapiAgentsUseCase()

        if(actor.requireIfi() !in authenticatedPerson.map { it.requireIfi() } )
            throw XapiException(400, "Agent IFI does not match authentication")
    }


    override suspend fun getMultipleDocuments(
        params: XapiStateResource.MultiDocParams,
        dataLoadParams: DataLoadParams
    ): DataLoadState<List<String>> {
        requireAuthenticatedPersonMatchesActor(params.agent)

        val stateIds = schoolDb.getStateDocumentDao().getStateIds(
            activityIri = params.activityId,
            agentIfi = params.agent.requireIfi(),
            registration = params.registration,
            since = params.since?.let { InstantAsTimestampString(it) },
        )
        return DataReadyState(stateIds)
    }

    override suspend fun get(
        params: XapiStateResource.SingleDocumentParams,
        dataLoadParams: DataLoadParams
    ): DataLoadState<XapiDocument> {
        return schoolDb.useReaderConnection { con ->
            requireAuthenticatedPersonMatchesActor(params.agent)
            con.withTransaction(Transactor.SQLiteTransactionType.DEFERRED) {
                val etagAndLastModifiedInDb = schoolDb.takeIf {
                    dataLoadParams.requestHeaders.hasIfNotModifiedHeaders()
                }?.getStateDocumentDao()
                    ?.findETagAndLastModifiedByActivityIriAndAgentIfiAndRegistrationAndStateId(
                        activityIri = params.activityId,
                        agentIfi = params.agent.requireIfi(),
                        registration = params.registration,
                        stateId = params.stateId,
                    )?.toModel()

                if (etagAndLastModifiedInDb != null &&
                    dataLoadParams.requestHeaders.requestEtagAndLastModified().isStillValid(
                        other = etagAndLastModifiedInDb
                    )
                ) {
                    return@withTransaction NoDataLoadedState.notModified()
                }

                val entity = schoolDb.getStateDocumentDao().findByActivityIriAndAgentIfiAndRegistrationAndStateId(
                    activityIri = params.activityId,
                    agentIfi = params.agent.requireIfi(),
                    registration = params.registration,
                    stateId = params.stateId,
                )

                if (entity != null) {
                    DataReadyState(
                        data = entity.document,
                        metaInfo = DataLoadMetaInfo(
                            lastModified = entity.document.updated.timestamp,
                            headers = headersOf(
                                HttpHeaders.LastModified to listOf(entity.document.updated.toHttpDate()),
                                HttpHeaders.ETag to listOf(entity.sha1.quote())
                            )
                        )
                    )
                } else {
                    NoDataLoadedState.notFound()
                }
            }
        }
    }

    override fun getAsFlow(
        params: XapiStateResource.SingleDocumentParams,
        dataLoadParams: DataLoadParams
    ): Flow<DataLoadState<XapiDocument>> {
        return schoolDb.invalidationTracker.createFlow(
            "state_document", emitInitialState = true
        ).map {
            get(params, dataLoadParams)
        }
    }

    override suspend fun post(
        params: XapiStateResource.SingleDocumentParams,
        document: XapiDocument
    ) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                if (!document.isJson())
                    throw XapiException(400, "Cannot post non-JSON document")

                requireAuthenticatedPersonMatchesActor(params.agent)

                val existing = schoolDb.getStateDocumentDao().findByActivityIriAndAgentIfiAndRegistrationAndStateId(
                    activityIri = params.activityId,
                    agentIfi = params.agent.requireIfi(),
                    registration = params.registration,
                    stateId = params.stateId,
                )
                val existingDoc = existing?.document

                if (existingDoc?.isJson() == false)
                    throw XapiException(400, "Cannot post when there is an existing non-JSON document")


                val entity = (existingDoc?.mergeJsonDoc(document, json) ?: document)
                    .toXapiStateDocumentEntity(
                        params = params,
                        id = existingDoc?.id ?: Uuid.random().toString()
                    )

                schoolDb.getStateDocumentDao().upsert(entity)
                schoolDb.getStateDocumentShaDao().upsert(
                    XapiStateDocumentShaEntity(
                        docId = entity.id,
                        sha1digest = sha1(entity.contents).toHexString(),
                    )
                )
            }
        }
    }

    override suspend fun put(
        params: XapiStateResource.SingleDocumentParams,
        document: XapiDocument
    ) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                requireAuthenticatedPersonMatchesActor(params.agent)

                val existing = schoolDb.getStateDocumentDao().findByActivityIriAndAgentIfiAndRegistrationAndStateId(
                    activityIri = params.activityId,
                    agentIfi = params.agent.requireIfi(),
                    registration = params.registration,
                    stateId = params.stateId,
                )
                val entity = document.toXapiStateDocumentEntity(
                    params = params,
                    id = existing?.document?.id,
                )
                schoolDb.getStateDocumentDao().upsert(entity)
                schoolDb.getStateDocumentShaDao().upsert(
                    XapiStateDocumentShaEntity(
                        docId = entity.id,
                        sha1digest = sha1(entity.contents).toHexString(),
                    )
                )
            }
        }
    }

    override suspend fun delete(params: XapiStateResource.SingleDocumentParams) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                requireAuthenticatedPersonMatchesActor(params.agent)

                val existing = schoolDb.getStateDocumentDao().findByActivityIriAndAgentIfiAndRegistrationAndStateId(
                    activityIri = params.activityId,
                    agentIfi = params.agent.requireIfi(),
                    registration = params.registration,
                    stateId = params.stateId,
                )
                existing?.document?.id?.let {
                    schoolDb.getStateDocumentShaDao().deleteByDocId(it)
                }
                schoolDb.getStateDocumentDao().deleteByActivityIriAndAgentIfiAndRegistrationAndStateId(
                    activityIri = params.activityId,
                    agentIfi = params.agent.requireIfi(),
                    registration = params.registration,
                    stateId = params.stateId,
                )
            }
        }
    }
}
