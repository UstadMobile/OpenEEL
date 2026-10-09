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

/**
 * Adds support for Xapi
 */
val MIGRATION_12_13 = object: Migration(12, 13) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `XapiActivityEntity` (`actUid` INTEGER NOT NULL, `actIdIri` TEXT NOT NULL, `actType` TEXT, `actMoreInfo` TEXT, `actInteractionType` INTEGER, `actCorrectResponsePatterns` TEXT, `actNonSignificantLastModified` INTEGER NOT NULL, `actSignificantLastModified` INTEGER NOT NULL, `actStored` INTEGER NOT NULL, `actFlags` INTEGER NOT NULL, `actObjectTypeSet` INTEGER NOT NULL, PRIMARY KEY(`actUid`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `XapiActivityExtensionEntity` (`aeeActivityUid` INTEGER NOT NULL, `aeeKeyHash` INTEGER NOT NULL, `aeeKey` TEXT NOT NULL, `aeeJson` TEXT NOT NULL, `aeeLastMod` INTEGER NOT NULL, `aeeIsDeleted` INTEGER NOT NULL, PRIMARY KEY(`aeeActivityUid`, `aeeKeyHash`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `XapiActivityInteractionEntity` (`aieUid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `aieActivityUid` INTEGER NOT NULL, `aieProp` INTEGER NOT NULL, `aieId` TEXT NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_XapiActivityInteractionEntity_aieActivityUid` ON `XapiActivityInteractionEntity` (`aieActivityUid`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `XapiActivityLangMapEntry` (`almeActivityUid` INTEGER NOT NULL, `almeKeyHash` INTEGER NOT NULL, `almeProperty` INTEGER NOT NULL, `almeInteractionId` TEXT, `almeLangCode` TEXT NOT NULL, `almeValue` TEXT NOT NULL, `almeLastModified` INTEGER NOT NULL, PRIMARY KEY(`almeActivityUid`, `almeKeyHash`))")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_XapiActivityLangMapEntry_almeActivityUid_almeProperty_almeInteractionId_almeLangCode` ON `XapiActivityLangMapEntry` (`almeActivityUid`, `almeProperty`, `almeInteractionId`, `almeLangCode`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `XapiActorEntity` (`actorUid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `actorPersonUid` INTEGER NOT NULL, `actorName` TEXT, `actorMbox` TEXT, `actorMbox_sha1sum` TEXT, `actorOpenid` TEXT, `actorAccountName` TEXT, `actorAccountHomePage` TEXT, `actorStored` INTEGER NOT NULL, `actorLastModified` INTEGER NOT NULL, `actorGroupMembersLastUpdated` INTEGER NOT NULL, `actorIsAnonGroup` INTEGER NOT NULL, `actorObjectType` INTEGER NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `idx_actorentity_uid_personuid` ON `XapiActorEntity` (`actorPersonUid`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `idx_actorentity_actorobjecttype` ON `XapiActorEntity` (`actorObjectType`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `idx_actorentity_actorIsAnonGroup` ON `XapiActorEntity` (`actorIsAnonGroup`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `XapiGroupMemberActorJoin` (`gmajGroupActorUid` INTEGER NOT NULL, `gmajMemberActorUid` INTEGER NOT NULL, PRIMARY KEY(`gmajGroupActorUid`, `gmajMemberActorUid`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `idx_groupmemberactorjoin_gmajgroupactoruid` ON `XapiGroupMemberActorJoin` (`gmajGroupActorUid`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `idx_groupmemberactorjoin_gmajmemberactoruid` ON `XapiGroupMemberActorJoin` (`gmajMemberActorUid`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `XapiStatementContextActivityJoin` (`scajUid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `scajFromStatementIdHi` INTEGER NOT NULL, `scajFromStatementIdLo` INTEGER NOT NULL, `scajContextType` INTEGER NOT NULL, `scajToActivityUid` INTEGER NOT NULL, `scajToActivityId` TEXT NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `idx_statementctx_stmt_id` ON `XapiStatementContextActivityJoin` (`scajFromStatementIdHi`, `scajFromStatementIdLo`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `XapiStatementEntity` (`statementIdHi` INTEGER NOT NULL, `statementIdLo` INTEGER NOT NULL, `statementVerbUid` INTEGER NOT NULL, `statementVerbId` TEXT NOT NULL, `statementObjectType` INTEGER NOT NULL, `statementObjectActivityId` TEXT, `statementObjectUid1` INTEGER NOT NULL, `statementObjectUid2` INTEGER NOT NULL, `statementActorUid` INTEGER NOT NULL, `authorityActorUid` INTEGER NOT NULL, `resultCompletion` INTEGER, `resultSuccess` INTEGER, `resultScoreScaled` REAL, `resultScoreRaw` REAL, `resultScoreMin` REAL, `resultScoreMax` REAL, `resultDuration` INTEGER, `resultResponse` TEXT, `resultExtensions` TEXT, `timestamp` INTEGER, `stored` INTEGER, `contextRegistrationHi` INTEGER NOT NULL, `contextRegistrationLo` INTEGER NOT NULL, `contextRegistrationHash` INTEGER NOT NULL, `contextLanguage` TEXT, `contextPlatform` TEXT, `contextRevision` TEXT, `contextStatementRefIdHi` INTEGER NOT NULL, `contextStatementRefIdLo` INTEGER NOT NULL, `contextInstructorActorUid` INTEGER NOT NULL, `contextTeamActorUid` INTEGER NOT NULL, `extensionProgress` INTEGER, `completionOrProgress` INTEGER NOT NULL, `isSubStatement` INTEGER NOT NULL, `statementVersion` TEXT, `stmtVoid` INTEGER NOT NULL, PRIMARY KEY(`statementIdHi`, `statementIdLo`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `idx_stmt_stored` ON `XapiStatementEntity` (`stored`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `idx_stmt_actor` ON `XapiStatementEntity` (`statementActorUid`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `idx_stmt_verb` ON `XapiStatementEntity` (`statementVerbUid`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `idx_stmt_obj1` ON `XapiStatementEntity` (`statementObjectUid1`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `idx_stmt_since` ON `XapiStatementEntity` (`stored`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `XapiStatementEntityJson` (`stmtJsonIdHi` INTEGER NOT NULL, `stmtJsonIdLo` INTEGER NOT NULL, `fullStatement` TEXT NOT NULL, PRIMARY KEY(`stmtJsonIdHi`, `stmtJsonIdLo`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `XapiVerbEntity` (`verbUid` INTEGER NOT NULL, `verbUrlId` TEXT NOT NULL, `verbStatus` INTEGER NOT NULL, `verbLct` INTEGER NOT NULL, PRIMARY KEY(`verbUid`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `XapiVerbLangMapEntry` (`vlmeVerbUid` INTEGER NOT NULL, `vlmeLangCode` TEXT NOT NULL, `vlmeEntryString` TEXT NOT NULL, PRIMARY KEY(`vlmeVerbUid`, `vlmeLangCode`))")
    }
}

val MIGRATION_13_14 = object: Migration(13, 14) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `XapiSessionEntity` (`xseUid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `xseRegistrationHi` INTEGER NOT NULL, `xseRegistrationLo` INTEGER NOT NULL, `xseAccountPersonUid` TEXT NOT NULL, `xseActorUid` INTEGER NOT NULL, `xseStartTime` INTEGER NOT NULL, `xseExpireTime` INTEGER NOT NULL, `xseAuth` TEXT NOT NULL)")
    }
}

val MIGRATION_14_15 = object: Migration(14, 15) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `XapiActivityExtensionEntity`")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `XapiActivityExtensionEntity` (`aeeActivityUid` INTEGER NOT NULL, `aeeKeyHash` INTEGER NOT NULL, `aeeKey` TEXT NOT NULL, `aeeJson` TEXT NOT NULL, `aeeLastMod` INTEGER NOT NULL, PRIMARY KEY(`aeeActivityUid`, `aeeKeyHash`))")
    }
}

val MIGRATION_15_16 = object: Migration(15, 16) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `XapiRemoteWriteQueueItemEntity` (`xrqItemId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `xrqMethod` INTEGER NOT NULL, `xrqResource` INTEGER NOT NULL, `xrqEntityItemId` TEXT NOT NULL, `xrqTimeQueued` INTEGER NOT NULL, `xrqAttemptCount` INTEGER NOT NULL, `xrqTimeWritten` INTEGER NOT NULL, `xrqAccountGuid` TEXT NOT NULL)")
    }
}

val MIGRATION_16_17 = object: Migration(16, 17) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `SchoolAppEntity`")
    }
}

val MIGRATION_17_18 = object: Migration(17, 18) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `activity_profile_document` (`id` TEXT NOT NULL, `profile_id` TEXT NOT NULL, `activity_iri` TEXT NOT NULL, `content_type` TEXT NOT NULL, `contents` BLOB NOT NULL, `content_length` INTEGER NOT NULL, `last_modified` TEXT NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `activity_profile_document_sha` (`doc_id` TEXT NOT NULL, `sha1_digest` TEXT NOT NULL, PRIMARY KEY(`doc_id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `state_document` (`id` TEXT NOT NULL, `state_id` TEXT NOT NULL, `activity_iri` TEXT NOT NULL, `agent_ifi` TEXT NOT NULL, `registration` TEXT, `content_type` TEXT NOT NULL, `contents` BLOB NOT NULL, `content_length` INTEGER NOT NULL, `last_modified` TEXT NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `state_document_sha` (`doc_id` TEXT NOT NULL, `sha1_digest` TEXT NOT NULL, PRIMARY KEY(`doc_id`))")
    }
}

val MIGRATION_18_19 = object: Migration(18, 19) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `agent_profile_document` (`id` TEXT NOT NULL, `profile_id` TEXT NOT NULL, `agent_ifi` TEXT NOT NULL, `content_type` TEXT NOT NULL, `contents` BLOB NOT NULL, `content_length` INTEGER NOT NULL, `last_modified` TEXT NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `agent_profile_document_sha` (`doc_id` TEXT NOT NULL, `sha1_digest` TEXT NOT NULL, PRIMARY KEY(`doc_id`))")
    }
}

val MIGRATION_19_20 = object: Migration(19, 20) {
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
        MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16,
        MIGRATION_16_17, MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20,
    )
}
