package org.openeel.datalayer.db

import kotlinx.serialization.json.Json
import org.openeel.datalayer.SchoolDirectoryDataSourceLocal
import org.openeel.datalayer.db.schooldirectory.SchoolDirectoryResourceDb
import org.openeel.datalayer.db.schooldirectory.SchoolDirectoryEntryResourceDb
import org.openeel.datalayer.schooldirectory.SchoolDirectoryResourceLocal
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryResourceLocal
import org.openeel.libxxhash.XXStringHasher

class SchoolDirectoryDataSourceDb(
    private val schoolDirectoryDatabase: SchoolDirectoryDatabase,
    private val json: Json,
    private val xxStringHasher: XXStringHasher,
): SchoolDirectoryDataSourceLocal {

    override val schoolDirectoryResource: SchoolDirectoryResourceLocal by lazy {
        SchoolDirectoryResourceDb(
            schoolDirectoryDatabase, xxStringHasher
        )
    }

    override val schoolDirectoryEntryResource: SchoolDirectoryEntryResourceLocal by lazy {
        SchoolDirectoryEntryResourceDb(
            schoolDirectoryDatabase, json, xxStringHasher
        )
    }
}