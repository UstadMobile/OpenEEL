package world.respect.datalayer.db.school.xapi

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
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.xapi.adapters.toXapiActivityProfileDocumentEntity
import world.respect.datalayer.db.school.xapi.entities.XapiActivityProfileDocumentShaEntity
import world.respect.datalayer.db.shared.InstantAsTimestampString
import world.respect.datalayer.db.shared.toModel
import world.respect.lib.dataloadstate.DataLoadMetaInfo
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.lib.dataloadstate.ext.hasIfNotModifiedHeaders
import world.respect.lib.dataloadstate.ext.isStillValid
import world.respect.lib.dataloadstate.ext.requestEtagAndLastModified
import world.respect.lib.xapi.exceptions.XapiException
import world.respect.lib.xapi.ext.isJson
import world.respect.lib.xapi.ext.mergeJsonDoc
import world.respect.lib.xapi.model.XapiDocument
import world.respect.lib.xapi.resources.XapiActivityProfileResource
import world.respect.lib.xapi.resources.local.XapiActivityProfileResourceLocal
import kotlin.uuid.Uuid

class XapiActivityProfileResourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val json: Json,
) : XapiActivityProfileResourceLocal {

    override suspend fun updateLocal(
        params: XapiActivityProfileResource.SingleDocumentParams,
        document: XapiDocument
    ) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val existing = schoolDb.getActivityProfileDocumentDao().findByActivityIriAndProfileId(
                    activityIri = params.activityId,
                    profileId = params.profileId,
                )
                val entity = document.toXapiActivityProfileDocumentEntity(
                    params = params,
                    id = existing?.document?.id,
                )

                schoolDb.getActivityProfileDocumentDao().upsert(entity)
                schoolDb.getActivityProfileDocumentShaDao().upsert(
                    XapiActivityProfileDocumentShaEntity(
                        docId = entity.id,
                        sha1digest = sha1(entity.contents).toHexString(),
                    )
                )
            }
        }

    }

    override suspend fun getMultipleDocuments(
        params: XapiActivityProfileResource.MultiDocParams,
        dataLoadParams: DataLoadParams
    ): DataLoadState<List<String>> {
        val profileIds = schoolDb.getActivityProfileDocumentDao().getProfileIds(
            activityIri = params.activityId,
            since = params.since?.let { InstantAsTimestampString(it) },
        )
        return DataReadyState(profileIds)
    }

    override suspend fun get(
        params: XapiActivityProfileResource.SingleDocumentParams,
        dataLoadParams: DataLoadParams
    ): DataLoadState<XapiDocument> {
        return schoolDb.useReaderConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.DEFERRED) {
                val etagAndLastModifiedInDb = schoolDb.takeIf {
                    dataLoadParams.requestHeaders.hasIfNotModifiedHeaders()
                }?.getActivityProfileDocumentDao()
                    ?.findETagAndLastModifiedByActivityIriAndProfileId(
                        activityIri = params.activityId,
                        profileId = params.profileId
                    )?.toModel()

                if(etagAndLastModifiedInDb != null &&
                    dataLoadParams.requestHeaders.requestEtagAndLastModified().isStillValid(
                        other = etagAndLastModifiedInDb
                    )
                ) {
                    return@withTransaction NoDataLoadedState.notModified()
                }

                val entity = schoolDb.getActivityProfileDocumentDao().findByActivityIriAndProfileId(
                    activityIri = params.activityId,
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
        params: XapiActivityProfileResource.SingleDocumentParams,
        dataLoadParams: DataLoadParams
    ): Flow<DataLoadState<XapiDocument>> {
        return schoolDb.invalidationTracker.createFlow(
            "activity_profile_document", emitInitialState = true
        ).map {
            get(params, dataLoadParams)
        }
    }

    override suspend fun post(
        params: XapiActivityProfileResource.SingleDocumentParams,
        document: XapiDocument
    ) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                if(!document.isJson())
                    throw XapiException(400, "Cannot post non-JSON document")

                val existing = schoolDb.getActivityProfileDocumentDao().findByActivityIriAndProfileId(
                    activityIri = params.activityId,
                    profileId = params.profileId,
                )
                val existingDoc = existing?.document

                if(existingDoc?.isJson() == false)
                    throw XapiException(400, "Cannot post when there is an existing non-JSON document")

                val entity = (existingDoc?.mergeJsonDoc(document, json) ?: document)
                    .toXapiActivityProfileDocumentEntity(
                        params = params,
                        id = existingDoc?.id ?: Uuid.random().toString()
                    )

                schoolDb.getActivityProfileDocumentDao().upsert(entity)
                schoolDb.getActivityProfileDocumentShaDao().upsert(
                    XapiActivityProfileDocumentShaEntity(
                        docId = entity.id,
                        sha1digest = sha1(entity.contents).toHexString(),
                    )
                )
            }
        }
    }

    override suspend fun put(
        params: XapiActivityProfileResource.SingleDocumentParams,
        document: XapiDocument
    ) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val existing = schoolDb.getActivityProfileDocumentDao().findByActivityIriAndProfileId(
                    activityIri = params.activityId,
                    profileId = params.profileId,
                )
                val entity = document.toXapiActivityProfileDocumentEntity(
                    params = params,
                    id = existing?.document?.id,
                )
                schoolDb.getActivityProfileDocumentDao().upsert(entity)
                schoolDb.getActivityProfileDocumentShaDao().upsert(
                    XapiActivityProfileDocumentShaEntity(
                        docId = entity.id,
                        sha1digest = sha1(entity.contents).toHexString(),
                    )
                )
            }
        }
    }

    override suspend fun delete(params: XapiActivityProfileResource.SingleDocumentParams) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val existing = schoolDb.getActivityProfileDocumentDao().findByActivityIriAndProfileId(
                    activityIri = params.activityId,
                    profileId = params.profileId,
                )
                existing?.document?.id?.let {
                    schoolDb.getActivityProfileDocumentShaDao().deleteByDocId(it)
                }
                schoolDb.getActivityProfileDocumentDao().deleteByActivityIriAndProfileId(
                    activityIri = params.activityId,
                    profileId = params.profileId,
                )
            }
        }
    }
}