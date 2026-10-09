package org.openeel.datalayer.db.schooldirectory

import androidx.room.Transactor
import androidx.room.useWriterConnection
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.datalayer.db.SchoolDirectoryDatabase
import org.openeel.datalayer.db.schooldirectory.adapters.toEntities
import org.openeel.datalayer.db.schooldirectory.adapters.toModel
import org.openeel.datalayer.respect.model.SchoolDirectoryEntry
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryResource
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryResourceLocal
import org.openeel.libxxhash.XXStringHasher

class SchoolDirectoryEntryResourceDb(
    private val respectAppDb: SchoolDirectoryDatabase,
    private val json: Json,
    private val xxStringHasher: XXStringHasher,
) : SchoolDirectoryEntryResourceLocal{

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
        listParams: SchoolDirectoryEntryResource.GetListParams
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
        listParams: SchoolDirectoryEntryResource.GetListParams
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