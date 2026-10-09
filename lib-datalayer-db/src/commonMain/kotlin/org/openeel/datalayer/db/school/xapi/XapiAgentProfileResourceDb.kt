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
import org.openeel.datalayer.db.school.xapi.adapters.toXapiAgentProfileDocumentEntity
import org.openeel.datalayer.db.school.xapi.entities.XapiAgentProfileDocumentShaEntity
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
import org.openeel.lib.xapi.resources.XapiAgentProfileResource
import org.openeel.lib.xapi.resources.local.XapiAgentProfileResourceLocal
import kotlin.collections.map
import kotlin.uuid.Uuid

class XapiAgentProfileResourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val json: Json,
    private val getAuthenticatedXapiAgentsUseCase: GetAuthenticatedXapiAgentsUseCase,
) : XapiAgentProfileResourceLocal {

    override suspend fun updateLocal(
        params: XapiAgentProfileResource.SingleDocumentParams,
        document: XapiDocument
    ) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val existing = schoolDb.getAgentProfileDocumentDao().findByAgentIfiAndProfileId(
                    agentIfi = params.agent.requireIfi(),
                    profileId = params.profileId,
                )
                val entity = document.toXapiAgentProfileDocumentEntity(
                    params = params,
                    id = existing?.document?.id,
                )

                schoolDb.getAgentProfileDocumentDao().upsert(entity)
                schoolDb.getAgentProfileDocumentShaDao().upsert(
                    XapiAgentProfileDocumentShaEntity(
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
        params: XapiAgentProfileResource.MultiDocParams,
        dataLoadParams: DataLoadParams
    ): DataLoadState<List<String>> {
        requireAuthenticatedPersonMatchesActor(params.agent)
        val profileIds = schoolDb.getAgentProfileDocumentDao().getProfileIds(
            agentIfi = params.agent.requireIfi(),
            since = params.since?.let { InstantAsTimestampString(it) },
        )
        return DataReadyState(profileIds)
    }

    override suspend fun get(
        params: XapiAgentProfileResource.SingleDocumentParams,
        dataLoadParams: DataLoadParams
    ): DataLoadState<XapiDocument> {
        return schoolDb.useReaderConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.DEFERRED) {
                requireAuthenticatedPersonMatchesActor(params.agent)

                val etagAndLastModifiedInDb = schoolDb.takeIf {
                    dataLoadParams.requestHeaders.hasIfNotModifiedHeaders()
                }?.getAgentProfileDocumentDao()
                    ?.findETagAndLastModifiedByAgentIfiAndProfileId(
                        agentIfi = params.agent.requireIfi(),
                        profileId = params.profileId
                    )?.toModel()

                if (etagAndLastModifiedInDb != null &&
                    dataLoadParams.requestHeaders.requestEtagAndLastModified().isStillValid(
                        other = etagAndLastModifiedInDb
                    )
                ) {
                    return@withTransaction NoDataLoadedState.notModified()
                }

                val entity = schoolDb.getAgentProfileDocumentDao().findByAgentIfiAndProfileId(
                    agentIfi = params.agent.requireIfi(),
                    profileId = params.profileId,
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
        params: XapiAgentProfileResource.SingleDocumentParams,
        dataLoadParams: DataLoadParams
    ): Flow<DataLoadState<XapiDocument>> {
        return schoolDb.invalidationTracker.createFlow(
            "agent_profile_document", emitInitialState = true
        ).map {
            get(params, dataLoadParams)
        }
    }

    override suspend fun post(
        params: XapiAgentProfileResource.SingleDocumentParams,
        document: XapiDocument
    ) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                requireAuthenticatedPersonMatchesActor(params.agent)

                if (!document.isJson())
                    throw XapiException(400, "Cannot post non-JSON document")

                val existing = schoolDb.getAgentProfileDocumentDao().findByAgentIfiAndProfileId(
                    agentIfi = params.agent.requireIfi(),
                    profileId = params.profileId,
                )
                val existingDoc = existing?.document

                if (existingDoc?.isJson() == false)
                    throw XapiException(400, "Cannot post when there is an existing non-JSON document")

                val entity = (existingDoc?.mergeJsonDoc(document, json) ?: document)
                    .toXapiAgentProfileDocumentEntity(
                        params = params,
                        id = existingDoc?.id ?: Uuid.random().toString()
                    )

                schoolDb.getAgentProfileDocumentDao().upsert(entity)
                schoolDb.getAgentProfileDocumentShaDao().upsert(
                    XapiAgentProfileDocumentShaEntity(
                        docId = entity.id,
                        sha1digest = sha1(entity.contents).toHexString(),
                    )
                )
            }
        }
    }

    override suspend fun put(
        params: XapiAgentProfileResource.SingleDocumentParams,
        document: XapiDocument
    ) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                requireAuthenticatedPersonMatchesActor(params.agent)

                val existing = schoolDb.getAgentProfileDocumentDao().findByAgentIfiAndProfileId(
                    agentIfi = params.agent.requireIfi(),
                    profileId = params.profileId,
                )
                val entity = document.toXapiAgentProfileDocumentEntity(
                    params = params,
                    id = existing?.document?.id,
                )
                schoolDb.getAgentProfileDocumentDao().upsert(entity)
                schoolDb.getAgentProfileDocumentShaDao().upsert(
                    XapiAgentProfileDocumentShaEntity(
                        docId = entity.id,
                        sha1digest = sha1(entity.contents).toHexString(),
                    )
                )
            }
        }
    }

    override suspend fun delete(params: XapiAgentProfileResource.SingleDocumentParams) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                requireAuthenticatedPersonMatchesActor(params.agent)

                val existing = schoolDb.getAgentProfileDocumentDao().findByAgentIfiAndProfileId(
                    agentIfi = params.agent.requireIfi(),
                    profileId = params.profileId,
                )
                existing?.document?.id?.let {
                    schoolDb.getAgentProfileDocumentShaDao().deleteByDocId(it)
                }
                schoolDb.getAgentProfileDocumentDao().deleteByAgentIfiAndProfileId(
                    agentIfi = params.agent.requireIfi(),
                    profileId = params.profileId,
                )
            }
        }
    }
}
