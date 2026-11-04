package org.openeel.datalayer.db

import kotlinx.serialization.json.Json
import org.openeel.datalayer.RespectAppDataSourceLocal
import org.openeel.datalayer.db.schooldirectory.SchoolDirectoryDataSourceDb
import org.openeel.datalayer.db.schooldirectory.SchoolDirectoryEntryDataSourceDb
import org.openeel.datalayer.schooldirectory.SchoolDirectoryDataSourceLocal
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryDataSourceLocal
import org.openeel.libxxhash.XXStringHasher

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