package org.openeel.datalayer.db

import kotlinx.serialization.json.Json
import org.openeel.datalayer.SchoolDirectoryDataSourceLocal
import org.openeel.datalayer.db.schooldirectory.SchoolDirectoryResourceDb
import org.openeel.datalayer.db.schooldirectory.SchoolDirectoryEntryResourceDb
import org.openeel.datalayer.schooldirectory.SchoolDirectoryResourceLocal
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryResourceLocal
import org.openeel.libxxhash.XXStringHasher

class SchoolDirectoryDataSourceDb(
    private val respectAppDatabase: RespectAppDatabase,
    private val json: Json,
    private val xxStringHasher: XXStringHasher,
): SchoolDirectoryDataSourceLocal {

    override val schoolDirectoryResource: SchoolDirectoryResourceLocal by lazy {
        SchoolDirectoryResourceDb(
            respectAppDatabase, xxStringHasher
        )
    }

    override val schoolDirectoryEntryResource: SchoolDirectoryEntryResourceLocal by lazy {
        SchoolDirectoryEntryResourceDb(
            respectAppDatabase, json, xxStringHasher
        )
    }
}