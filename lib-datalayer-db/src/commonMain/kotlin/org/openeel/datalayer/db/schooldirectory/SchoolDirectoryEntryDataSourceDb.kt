package world.respect.datalayer.db.schooldirectory

import androidx.room.Transactor
import androidx.room.useWriterConnection
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.datalayer.db.RespectAppDatabase
import world.respect.datalayer.db.schooldirectory.adapters.toEntities
import world.respect.datalayer.db.schooldirectory.adapters.toModel
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSourceLocal
import world.respect.libxxhash.XXStringHasher

class SchoolDirectoryEntryDataSourceDb(
    private val respectAppDb: RespectAppDatabase,
    private val json: Json,
    private val xxStringHasher: XXStringHasher,
) : SchoolDirectoryEntryDataSourceLocal{

    override suspend fun updateLocal(
        list: List<SchoolDirectoryEntry>,
        forceOverwrite: Boolean
    ) {
        respectAppDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                list.forEach { item ->
                    val entities = item.toEntities(xxStringHasher)
                    respectAppDb.getSchoolDirectoryEntryLangMapEntityDao().deleteByFk(
                        entities.school.reUid
                    )

                    respectAppDb.getSchoolDirectoryEntryAuthOptionEntityDao().deleteByFk(
                        entities.school.reUid
                    )

                    respectAppDb.getSchoolDirectoryEntryEntityDao().upsert(entities.school)
                    respectAppDb.getSchoolDirectoryEntryLangMapEntityDao().upsert(
                        entities.langMapEntities
                    )
                    respectAppDb.getSchoolDirectoryEntryAuthOptionEntityDao().upsert(
                        entities.authOptionEntities
                    )
                }
            }
        }
    }

    override suspend fun findByUidList(uids: List<String>): List<SchoolDirectoryEntry> {
        throw IllegalStateException("findByUidList: should not be used here, this does not support remote write queue")
    }

    override fun listAsFlow(
        loadParams: DataLoadParams,
        listParams: SchoolDirectoryEntryDataSource.GetListParams
    ): Flow<DataLoadState<List<SchoolDirectoryEntry>>> {
        return respectAppDb.getSchoolDirectoryEntryEntityDao().listAsFlow(
            name = listParams.name?.let { "%$it%" },
            directoryUrl = listParams.directoryUrl?.toString(),
        ).map { list ->
            DataReadyState(
                data = list.map { it.toModel() },
            )
        }
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        listParams: SchoolDirectoryEntryDataSource.GetListParams
    ): DataLoadState<List<SchoolDirectoryEntry>> {
        return DataReadyState(
            respectAppDb.getSchoolDirectoryEntryEntityDao().list(
                name = listParams.name?.let { "%$it%" },
                directoryUrl = listParams.directoryUrl?.toString(),
            ).map { it.toModel() }
        )
    }

    override suspend fun getSchoolDirectoryEntryByUrl(url: Url): DataLoadState<SchoolDirectoryEntry> {
        return respectAppDb.getSchoolDirectoryEntryEntityDao().findByUid(
            xxStringHasher.hash(url.toString())
        )?.let {
            DataReadyState(it.toModel())
        } ?: NoDataLoadedState.notFound()
    }
}