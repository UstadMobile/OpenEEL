package world.respect.datalayer.db.school.opds

import androidx.room.Transactor
import androidx.room.useWriterConnection
import com.ustadmobile.ihttp.headers.IHttpHeaders
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.opds.adapters.OpdsFeedEntities
import world.respect.datalayer.db.school.opds.adapters.asEntities
import world.respect.datalayer.db.school.opds.adapters.asModel
import world.respect.datalayer.db.school.opds.entities.OpdsFeedEntity
import world.respect.datalayer.db.school.opds.ext.etagAndLastModified
import world.respect.datalayer.db.shared.adapters.asNetworkValidationInfo
import world.respect.datalayer.ext.EPOCH
import world.respect.datalayer.networkvalidation.NetworkValidationInfo
import world.respect.datalayer.school.opds.OpdsFeedDataSourceLocal
import world.respect.datalayer.school.opds.ext.requireSelfUrl
import world.respect.lib.dataloadstate.DataLoadMetaInfo
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.lib.dataloadstate.ext.requestEtagAndLastModified
import world.respect.lib.dataloadstate.ext.isStillValid
import world.respect.lib.dataloadstate.ext.toResponseHeaders
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.primarykeygen.PrimaryKeyGenerator
import kotlin.time.Clock

class OpdsFeedDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    @Suppress("unused")
    private val authenticatedUser: AuthenticatedUserPrincipalId,
    private val json: Json,
    private val primaryKeyGenerator: PrimaryKeyGenerator,
) : OpdsFeedDataSourceLocal{

    private suspend fun OpdsFeedEntity?.toDataLoadState(
        url: Url,
        params: DataLoadParams,
    ): DataLoadState<OpdsFeed> {
        return when {
            this != null && params.requestHeaders.requestEtagAndLastModified().isStillValid(
                other = this.etagAndLastModified()
            ) -> {
                NoDataLoadedState.notModified(
                    metaInfo = DataLoadMetaInfo(
                        url = url,
                        headers = this.etagAndLastModified().toResponseHeaders()
                    )
                )
            }

            this != null -> {
                DataReadyState(
                    data = OpdsFeedEntities(
                        opdsFeed = this,
                        feedMetaData = schoolDb.getOpdsFeedMetadataEntityDao().findByFeedUid(this.ofeUid),
                        langMapEntities = schoolDb.getLangMapEntityDao().findAllByFeedUid(this.ofeUid),
                        linkEntities =schoolDb.getReadiumLinkEntityDao().findAllByFeedUid(this.ofeUid),
                        publications = schoolDb.getOpdsPublicationEntityDao().findByFeedUid(
                            this.ofeUid),
                        groups = schoolDb.getOpdsGroupEntityDao().findByFeedUid(this.ofeUid),
                        subjects = schoolDb.getReadiumSubjectEntityDao().findAllByFeedUid(this.ofeUid),
                    ).asModel(json),
                    metaInfo = DataLoadMetaInfo(
                        url = url,
                        headers = this.etagAndLastModified().toResponseHeaders()
                    ),
                )
            }

            else -> {
                NoDataLoadedState.notFound()
            }
        }
    }

    override fun getByUrlAsFlow(
        url: Url,
        params: DataLoadParams,
    ): Flow<DataLoadState<OpdsFeed>> {
        return schoolDb.getOpdsFeedEntityDao().findByUrlHashAsFlow(
            uidNumberMapper(url.toString())
        ).map { feedEntity ->
            feedEntity.toDataLoadState(url, params)
        }
    }

    override suspend fun getByUrl(
        url: Url,
        params: DataLoadParams
    ): DataLoadState<OpdsFeed> {
        return schoolDb.getOpdsFeedEntityDao().findByUrlHash(
            urlHash = uidNumberMapper(url.toString())
        ).toDataLoadState(url, params)
    }

    override suspend fun getValidationInfo(
        url: Url,
        requestHeaders: IHttpHeaders
    ): NetworkValidationInfo? {
        return schoolDb.getOpdsFeedEntityDao().getNetworkValidationInfo(
            urlHash = uidNumberMapper(url.toString()),
        )?.asNetworkValidationInfo()
    }

    private suspend fun doUpsertOpdsFeed(
        opdsFeed: OpdsFeed,
        dataLoadMetaInfo: DataLoadMetaInfo,
    ) {
        val feedEntities = opdsFeed.asEntities(
            dataLoadMetaInfo =  dataLoadMetaInfo,
            json = json,
            primaryKeyGenerator = primaryKeyGenerator,
            uidNumberMapper = uidNumberMapper,
        )
        val feedUrl = opdsFeed.requireSelfUrl()

        val hasMetaData = feedEntities.feedMetaData.any {
            it.ofmeOfeUid == feedEntities.opdsFeed.ofeUid
        }

        if(!hasMetaData) {
            throw IllegalStateException("WTF: feedEntities has no metadata for opdsfeed")
        }

        val feedUid = uidNumberMapper(feedUrl.toString())
        schoolDb.getOpdsFeedEntityDao().deleteByFeedUid(feedUid)
        schoolDb.getOpdsFeedMetadataEntityDao().deleteByFeedUid(feedUid)
        schoolDb.getLangMapEntityDao().deleteAllByFeedUid(feedUid)
        schoolDb.getReadiumLinkEntityDao().deleteAllByFeedUid(feedUid)
        schoolDb.getOpdsPublicationEntityDao().deleteAllByFeedUid(feedUid)
        schoolDb.getOpdsGroupEntityDao().deleteByFeedUid(feedUid)

        schoolDb.getOpdsFeedEntityDao().insertList(
            listOf(feedEntities.opdsFeed.copy(ofeStored = Clock.System.now()))
        )
        schoolDb.getOpdsFeedMetadataEntityDao().insertList(feedEntities.feedMetaData)
        schoolDb.getLangMapEntityDao().insertAsync(feedEntities.langMapEntities)
        schoolDb.getReadiumLinkEntityDao().insertList(feedEntities.linkEntities)
        schoolDb.getOpdsPublicationEntityDao().insertList(feedEntities.publications)
        schoolDb.getOpdsGroupEntityDao().insertList(feedEntities.groups)
    }


    override suspend fun updateLocal(
        url: Url,
        dataLoadResult: DataReadyState<OpdsFeed>,
    ) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val opdsFeedEntity = schoolDb.getOpdsFeedEntityDao().findByUrlHash(
                    uidNumberMapper(url.toString())
                )
                val dbLastModified = opdsFeedEntity?.ofeLastModified ?: EPOCH
                val isMoreRecent = dataLoadResult.data.metadata.modified?.let {
                    it > dbLastModified
                } ?: true

                if(isMoreRecent) {
                    doUpsertOpdsFeed(
                        opdsFeed = dataLoadResult.data,
                        dataLoadMetaInfo = dataLoadResult.metaInfo,
                    )
                }
            }
        }
    }

}