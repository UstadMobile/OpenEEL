package world.respect.datalayer.db.school.opds

import androidx.room.Transactor
import androidx.room.useReaderConnection
import androidx.room.useWriterConnection
import com.ustadmobile.ihttp.headers.IHttpHeaders
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.opds.adapters.OpdsPublicationEntities
import world.respect.datalayer.db.school.opds.adapters.asEntities
import world.respect.datalayer.db.school.opds.adapters.asModel
import world.respect.datalayer.db.school.opds.entities.OpdsPublicationEntity
import world.respect.datalayer.db.shared.adapters.asNetworkValidationInfo
import world.respect.datalayer.db.shared.entities.LangMapEntity
import world.respect.datalayer.networkvalidation.BaseDataSourceValidationHelper
import world.respect.datalayer.networkvalidation.NetworkValidationInfo
import world.respect.datalayer.school.opds.OpdsPublicationDataSourceLocal
import world.respect.lib.opds.model.Publication
import world.respect.lib.primarykeygen.PrimaryKeyGenerator

class OpdsPublicationDataSourceDb(
    private val respectSchoolDatabase: RespectSchoolDatabase,
    private val json: Json,
    private val uidNumberMapper: UidNumberMapper,
    private val primaryKeyGenerator: PrimaryKeyGenerator,
): OpdsPublicationDataSourceLocal {

    override val publicationNetworkValidationHelper = object: BaseDataSourceValidationHelper {
        override suspend fun getValidationInfo(
            url: Url,
            requestHeaders: IHttpHeaders,
        ): NetworkValidationInfo? {
            return respectSchoolDatabase.getOpdsPublicationEntityDao().getLastModifiedAndETag(
                uidNumberMapper(url.toString())
            )?.asNetworkValidationInfo()
        }
    }

    override suspend fun updateOpdsPublication(publication: DataReadyState<Publication>) {
        val pubData = publication.data
        val url = publication.metaInfo.requireUrl()

        val publicationEntities = pubData.asEntities(
            dataLoadResult = publication,
            primaryKeyGenerator = primaryKeyGenerator,
            json = json,
            uidNumberMapper = uidNumberMapper,
            feedUid = 0,
            groupUid = 0,
            feedIndex = 0,
        )

        respectSchoolDatabase.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val oldPubUid = respectSchoolDatabase.getOpdsPublicationEntityDao().getUidByUrlHash(
                    uidNumberMapper(url.toString())
                )

                respectSchoolDatabase.getLangMapEntityDao().deleteByTableAndTopParentType(
                    lmeTopParentType = LangMapEntity.TopParentType.OPDS_PUBLICATION.id,
                    lmeEntityUid1 = oldPubUid,
                )
                respectSchoolDatabase.getReadiumLinkEntityDao().deleteAllByPublicationUid(oldPubUid)
                respectSchoolDatabase.getOpdsPublicationEntityDao().deleteByUid(oldPubUid)
                respectSchoolDatabase.getReadiumSubjectEntityDao().deleteAllByPublicationUid(oldPubUid)

                respectSchoolDatabase.getOpdsPublicationEntityDao().insertList(
                    listOf(publicationEntities.opdsPublicationEntity)
                )
                respectSchoolDatabase.getLangMapEntityDao().insertAsync(publicationEntities.langMapEntities)
                respectSchoolDatabase.getReadiumLinkEntityDao().insertList(publicationEntities.linkEntities)
                respectSchoolDatabase.getReadiumSubjectEntityDao().insertList(publicationEntities.subjectEntities)
            }
        }
    }

    suspend fun OpdsPublicationEntity.loadPublicationEntities(): OpdsPublicationEntities {
        return OpdsPublicationEntities(
            opdsPublicationEntity = this,
            langMapEntities = respectSchoolDatabase.getLangMapEntityDao().selectAllByTableAndEntityId(
                lmeTopParentType = LangMapEntity.TopParentType.OPDS_PUBLICATION.id,
                lmeEntityUid1 = this.opeUid,
                lmeEntityUid2 = 0
            ),
            linkEntities = respectSchoolDatabase.getReadiumLinkEntityDao().findAllByPubUid(
                this.opeUid
            ),
            subjectEntities = respectSchoolDatabase.getReadiumSubjectEntityDao().findAllByPubUid(
                this.opeUid
            ),
        )
    }

    override fun getByUrlAsFlow(
        url: Url,
        params: DataLoadParams,
        referrerUrl: Url?,
        expectedPublicationId: String?
    ): Flow<DataLoadState<Publication>> {
        val urlHash = uidNumberMapper(url.toString())

        return respectSchoolDatabase.getOpdsPublicationEntityDao().findByUrlHashAsFlow(urlHash).map { entity ->
            respectSchoolDatabase.useReaderConnection {
                entity?.loadPublicationEntities()?.asModel(json) ?: NoDataLoadedState.notFound()
            }
        }
    }

    override suspend fun getByUrl(
        url: Url,
        params: DataLoadParams,
        referrerUrl: Url?,
        expectedPublicationId: String?
    ) : DataLoadState<Publication> {
    return respectSchoolDatabase.useReaderConnection {
            respectSchoolDatabase.getOpdsPublicationEntityDao().findByUrlHash(
                uidNumberMapper(url.toString())
            )?.loadPublicationEntities()?.asModel(json) ?: NoDataLoadedState.notFound()
        }
    }

}