package world.respect.datalayer.db.schooldirectory

import androidx.room.Transactor
import androidx.room.useWriterConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.db.RespectAppDatabase
import world.respect.datalayer.db.schooldirectory.adapters.toEntity
import world.respect.datalayer.db.schooldirectory.adapters.toModel
import world.respect.datalayer.db.schooldirectory.entities.SchoolConfigEntity
import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSourceLocal
import world.respect.libxxhash.XXStringHasher

class SchoolDirectoryDataSourceDb(
    private val respectAppDb: RespectAppDatabase,
    private val xxStringHasher: XXStringHasher,
) : SchoolDirectoryDataSourceLocal {

    override suspend fun allDirectories(): List<RespectSchoolDirectory> {
        return respectAppDb.getSchoolDirectoryEntityDao().getSchoolDirectories().map { it.toModel() }
    }

    override fun allDirectoriesAsFlow(): Flow<List<RespectSchoolDirectory>> {
        return respectAppDb.getSchoolDirectoryEntityDao().getSchoolDirectoriesAsFlow().map { list ->
            list.map { it.toModel() }
        }
    }

    override suspend fun setServerManagedSchoolConfig(
        school: SchoolDirectoryEntry,
        dbUrl: String,
    ) {
        respectAppDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                respectAppDb.getSchoolConfigEntityDao().upsert(
                    SchoolConfigEntity(
                        rcUid = xxStringHasher.hash(school.self.toString()),
                        dbUrl = dbUrl,
                    )
                )
            }
        }
    }

    override suspend fun insertOrIgnore(
        schoolDirectory: RespectSchoolDirectory,
        clearOthers: Boolean,
    ) {
        val directoryUidNum = xxStringHasher.hash(schoolDirectory.baseUrl.toString())
        respectAppDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                respectAppDb.getSchoolDirectoryEntityDao().insertOrIgnore(
                    schoolDirectory.toEntity(xxStringHasher)
                )

                respectAppDb.takeIf { clearOthers }?.getSchoolDirectoryEntityDao()
                    ?.deleteOthers(exceptUid = directoryUidNum)
            }
        }
    }

    override suspend fun deleteDirectory(directory: RespectSchoolDirectory) {
        respectAppDb.getSchoolDirectoryEntityDao().deleteByUrl(directory.baseUrl.toString())
    }

}