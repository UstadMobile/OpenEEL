package world.respect.datalayer.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import world.respect.datalayer.db.networkvalidation.daos.NetworkValidationInfoEntityDao
import world.respect.datalayer.db.networkvalidation.entities.NetworkValidationInfoEntity
import world.respect.datalayer.db.schooldirectory.daos.SchoolConfigEntityDao
import world.respect.datalayer.db.schooldirectory.daos.SchoolDirectoryEntityDao
import world.respect.datalayer.db.schooldirectory.daos.SchoolDirectoryEntryAuthOptionEntityDao
import world.respect.datalayer.db.schooldirectory.daos.SchoolDirectoryEntryEntityDao
import world.respect.datalayer.db.schooldirectory.daos.SchoolDirectoryEntryLangMapEntityDao
import world.respect.datalayer.db.schooldirectory.entities.SchoolConfigEntity
import world.respect.datalayer.db.schooldirectory.entities.SchoolDirectoryEntity
import world.respect.datalayer.db.schooldirectory.entities.SchoolDirectoryEntryAuthOptionEntity
import world.respect.datalayer.db.schooldirectory.entities.SchoolDirectoryEntryEntity
import world.respect.datalayer.db.schooldirectory.entities.SchoolDirectoryEntryLangMapEntity
import world.respect.datalayer.db.shared.SharedConverters

@Database(
    entities = [
        //SchoolDirectory
        SchoolDirectoryEntity::class,
        SchoolDirectoryEntryEntity::class,
        SchoolDirectoryEntryLangMapEntity::class,
        SchoolConfigEntity::class,
        SchoolDirectoryEntryAuthOptionEntity::class,

        //Network validation
        NetworkValidationInfoEntity::class,
    ],
    version = 11,
)
@TypeConverters(SharedConverters::class)
@ConstructedBy(RespectAppDatabaseConstructor::class)
abstract class RespectAppDatabase : RoomDatabase() {

    abstract fun getSchoolDirectoryEntryEntityDao(): SchoolDirectoryEntryEntityDao

    abstract fun getSchoolDirectoryEntryLangMapEntityDao(): SchoolDirectoryEntryLangMapEntityDao

    abstract fun getSchoolDirectoryEntryAuthOptionEntityDao(): SchoolDirectoryEntryAuthOptionEntityDao

    abstract fun getSchoolConfigEntityDao(): SchoolConfigEntityDao

    abstract fun getSchoolDirectoryEntityDao(): SchoolDirectoryEntityDao

    abstract fun getNetworkValidationInfoEntityDao(): NetworkValidationInfoEntityDao

}

// The Room compiler generates the `actual` implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING",
    "KotlinNoActualForExpect", "RedundantSuppression"
)
expect object RespectAppDatabaseConstructor : RoomDatabaseConstructor<RespectAppDatabase> {
    override fun initialize(): RespectAppDatabase
}
