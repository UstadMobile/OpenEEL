package org.openeel.datalayer.db.schooldirectory

import androidx.room.Transactor
import androidx.room.useWriterConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.openeel.datalayer.db.SchoolDirectoryDatabase
import org.openeel.datalayer.db.schooldirectory.adapters.toEntity
import org.openeel.datalayer.db.schooldirectory.adapters.toModel
import org.openeel.datalayer.db.schooldirectory.entities.SchoolConfigEntity
import org.openeel.datalayer.respect.model.SchoolDirectory
import org.openeel.datalayer.respect.model.SchoolDirectoryEntry
import org.openeel.datalayer.schooldirectory.SchoolDirectoryResourceLocal
import org.openeel.libxxhash.XXStringHasher

class SchoolDirectoryResourceDb(
    private val respectAppDb: SchoolDirectoryDatabase,
    private val xxStringHasher: XXStringHasher,
) : SchoolDirectoryResourceLocal {

    override suspend fun allDirectories(): List<SchoolDirectory> {
        return respectAppDb.getSchoolDirectoryEntityDao().getSchoolDirectories().map { it.toModel() }
    }

    override fun allDirectoriesAsFlow(): Flow<List<SchoolDirectory>> {
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
        schoolDirectory: SchoolDirectory,
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

    override suspend fun deleteDirectory(directory: SchoolDirectory) {
        respectAppDb.getSchoolDirectoryEntityDao().deleteByUrl(directory.baseUrl.toString())
    }

}