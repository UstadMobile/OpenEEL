package world.respect.datalayer.db

import kotlinx.serialization.json.Json
import world.respect.datalayer.RespectAppDataSourceLocal
import world.respect.datalayer.db.schooldirectory.SchoolDirectoryDataSourceDb
import world.respect.datalayer.db.schooldirectory.SchoolDirectoryEntryDataSourceDb
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSourceLocal
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSourceLocal
import world.respect.libxxhash.XXStringHasher

class RespectAppDataSourceDb(
    private val respectAppDatabase: RespectAppDatabase,
    private val json: Json,
    private val xxStringHasher: XXStringHasher,
): RespectAppDataSourceLocal {

    override val schoolDirectoryDataSource: SchoolDirectoryDataSourceLocal by lazy {
        SchoolDirectoryDataSourceDb(
            respectAppDatabase, xxStringHasher
        )
    }

    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSourceLocal by lazy {
        SchoolDirectoryEntryDataSourceDb(
            respectAppDatabase, json, xxStringHasher
        )
    }
}