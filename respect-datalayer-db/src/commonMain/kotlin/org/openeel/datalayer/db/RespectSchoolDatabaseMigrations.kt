package world.respect.datalayer.db

import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val MIGRATION_11_12 = object: Migration(11, 12) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `LangMapEntity` (`lmeId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `lmeTopParentType` INTEGER NOT NULL, `lmeTopParentUid1` INTEGER NOT NULL, `lmeTopParentUid2` INTEGER NOT NULL, `lmePropType` INTEGER NOT NULL, `lmePropFk` INTEGER NOT NULL, `lmeLang` TEXT NOT NULL, `lmeRegion` TEXT, `lmeValue` TEXT NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_LangMapEntity_lmeTopParentType_lmeTopParentUid1_lmeTopParentUid2_lmePropType` ON `LangMapEntity` (`lmeTopParentType`, `lmeTopParentUid1`, `lmeTopParentUid2`, `lmePropType`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `ReadiumLinkEntity` (`rleId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `rleOpdsParentType` INTEGER NOT NULL, `rleOpdsParentUid` INTEGER NOT NULL, `rlePropType` TEXT NOT NULL, `rlePropFk` INTEGER NOT NULL, `rleIndex` INTEGER NOT NULL, `rleHref` TEXT NOT NULL, `rleRel` TEXT, `rleType` TEXT, `rleTitle` TEXT, `rleTemplated` INTEGER, `rleProperties` TEXT, `rleHeight` INTEGER, `rleWidth` INTEGER, `rleSize` INTEGER, `rleBitrate` REAL, `rleDuration` REAL, `rleLanguage` TEXT)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `OpdsPublicationEntity` (`opeUid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `opeOfeUid` INTEGER NOT NULL, `opeOgeUid` INTEGER NOT NULL, `opeIndex` INTEGER NOT NULL, `opeUrl` TEXT, `opeUrlHash` INTEGER NOT NULL, `opeLastModified` INTEGER NOT NULL, `opeEtag` TEXT, `opeMdIdentifier` TEXT, `opeMdLanguage` TEXT, `opeMdType` TEXT, `opeMdDescription` TEXT, `opeMdNumberOfPages` INTEGER, `opeMdDuration` REAL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `ReadiumSubjectEntity` (`rseUid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `rseStringValue` TEXT, `rseTopParentType` INTEGER NOT NULL, `rseTopParentUid` INTEGER NOT NULL, `rseSubjectSortAs` TEXT, `rseSubjectCode` TEXT, `rseSubjectScheme` TEXT, `rseIndex` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `OpdsFacetEntity` (`ofaeUid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `ofaeOfeUid` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `OpdsGroupEntity` (`ogeUid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `ogeOfeUid` INTEGER NOT NULL, `ogeIndex` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `OpdsFeedEntity` (`ofeUid` INTEGER NOT NULL, `ofeUrl` TEXT NOT NULL, `ofeUrlHash` INTEGER NOT NULL, `ofeLastModified` INTEGER NOT NULL, `ofeLastModifiedHeader` INTEGER NOT NULL, `ofeEtag` TEXT, `ofeStored` INTEGER NOT NULL, PRIMARY KEY(`ofeUid`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `OpdsFeedMetadataEntity` (`ofmeUid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `ofmeOfeUid` INTEGER NOT NULL, `ofmePropType` INTEGER NOT NULL, `ofmePropFk` INTEGER NOT NULL, `ofmeIdentifier` TEXT, `ofmeType` TEXT, `ofmeTitle` TEXT NOT NULL, `ofmeSubtitle` TEXT, `ofmeModified` INTEGER, `ofmeDescription` TEXT, `ofmeItemsPerPage` INTEGER, `ofmeCurrentPage` INTEGER, `ofmeNumberOfItems` INTEGER)")
    }
}

val MIGRATION_12_13 = object: Migration(12, 13) {
    override fun migrate(connection: SQLiteConnection) {

        connection.execSQL("UPDATE PersonRoleEntity SET prRoleEnum = CASE WHEN prRoleEnum = 3 THEN 4 WHEN prRoleEnum = 4 THEN 8 WHEN prRoleEnum = 5 THEN 16 ELSE prRoleEnum END WHERE prRoleEnum IN (3, 4, 5)")

        connection.execSQL("UPDATE SchoolPermissionGrantEntity SET spgToRole = CASE WHEN spgToRole = 3 THEN 4 WHEN spgToRole = 4 THEN 8 WHEN spgToRole = 5 THEN 16 ELSE spgToRole END WHERE spgToRole IN (3, 4, 5)")

        connection.execSQL("UPDATE InviteEntity SET iNewUserRole = CASE WHEN iNewUserRole = 3 THEN 4 WHEN iNewUserRole = 4 THEN 8 WHEN iNewUserRole = 5 THEN 16 ELSE iNewUserRole END WHERE iNewUserRole IN (3, 4, 5)")

        connection.execSQL("CREATE TABLE IF NOT EXISTS `SchoolConfigSettingEntity` (`scsKey` TEXT NOT NULL, `scsValue` TEXT NOT NULL, `scsStatus` INTEGER NOT NULL, `scsLastModified` INTEGER NOT NULL, `scsStored` INTEGER NOT NULL, `scsCanReadFlags` INTEGER NOT NULL, `scsAnonCanRead` INTEGER NOT NULL, `scsCanWriteFlags` INTEGER NOT NULL, PRIMARY KEY(`scsKey`))")
    }
}

fun RoomDatabase.Builder<RespectSchoolDatabase>.addCommonMigrations(

): RoomDatabase.Builder<RespectSchoolDatabase> {
    return this.addMigrations(
        MIGRATION_11_12,
        MIGRATION_12_13,
    )
}
