package world.respect.datalayer.db

import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL


val APP_MIGRATION_2_3 = object: Migration(2,3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS PersonPasskeyEntity")
        listOf(
            "LangMapEntity",
            "ReadiumLinkEntity",
            "OpdsPublicationEntity",
            "ReadiumSubjectEntity",
            "OpdsFacetEntity",
            "OpdsGroupEntity",
            "OpdsFeedEntity",
            "OpdsFeedMetadataEntity",
        ).forEach {
            connection.execSQL("DROP TABLE IF EXISTS $it")
        }
    }
}

val APP_MIGRATION_3_4 = object: Migration(3,4) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS CompatibleAppEntity")
        connection.execSQL("DROP TABLE IF EXISTS CompatibleAppAddJoin")
    }
}

val APP_MIGRATION_4_5 = object: Migration(4, 5) {

    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE SchoolDirectoryEntryEntity ADD COLUMN reInDirectoryUrl TEXT")
    }
}

val APP_MIGRATION_5_6 = object: Migration(5, 6) {

    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE SchoolDirectoryEntity ADD COLUMN rdName TEXT")
    }
}

val APP_MIGRATION_8_9_CLIENT = object: Migration(8, 9) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("""
            INSERT OR IGNORE INTO SchoolDirectoryEntity(rdUid, rdUrl, rdInvitePrefix, rdName)
            VALUES(6401554181369316842, 'https://respect-testing.ustadmobile.com/', 'Demos and review', 'Demos and review')
        """)
    }
}

val APP_MIGRATION_8_9_SERVER = object: Migration(8, 9) {
    override fun migrate(connection: SQLiteConnection) {
        //Do nothing - directories are not tracked on server.
    }
}

val APP_MIGRATION_9_10 = object: Migration(9, 10) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `SchoolDirectoryEntryEntity_new` (`reUid` INTEGER NOT NULL, `reSelf` TEXT NOT NULL, `reXapi` TEXT NOT NULL, `reRespectExt` TEXT, `reRpId` TEXT, `reLastModified` INTEGER NOT NULL, `reStored` INTEGER NOT NULL, `reInDirectoryUrl` TEXT, PRIMARY KEY(`reUid`))")
        connection.execSQL("INSERT INTO `SchoolDirectoryEntryEntity_new` (`reUid`, `reSelf`, `reXapi`, `reRespectExt`, `reRpId`, `reLastModified`, `reStored`, `reInDirectoryUrl`) SELECT `reUid`, `reSelf`, `reXapi`, `reRespectExt`, `reRpId`, `reLastModified`, `reStored`, `reInDirectoryUrl` FROM `SchoolDirectoryEntryEntity`")
        connection.execSQL("DROP TABLE `SchoolDirectoryEntryEntity`")
        connection.execSQL("ALTER TABLE `SchoolDirectoryEntryEntity_new` RENAME TO `SchoolDirectoryEntryEntity`")
    }
}

val APP_MIGRATION_10_11 = object: Migration(10, 11) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `SchoolDirectoryEntryAuthOptionEntity` (`sdeAoUid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sdeAoRdUid` INTEGER NOT NULL, `sdeAoName` TEXT NOT NULL, `sdeAoConfigType` TEXT NOT NULL, `sdeAoOpenIdIssuerUrl` TEXT)")
    }
}

fun RoomDatabase.Builder<RespectAppDatabase>.addCommonMigrations(

): RoomDatabase.Builder<RespectAppDatabase> {
    return this.addMigrations(
        APP_MIGRATION_2_3,
        APP_MIGRATION_3_4,
        APP_MIGRATION_4_5,
        APP_MIGRATION_5_6,
        APP_MIGRATION_9_10,
        APP_MIGRATION_10_11,
    )
}


