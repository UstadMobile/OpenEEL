package world.respect.datalayer.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.ustadmobile.core.db.dao.xapi.XapiActivityLangMapEntryDao
import world.respect.datalayer.db.school.opds.OpdsTypeConverters
import world.respect.datalayer.db.school.opds.daos.OpdsFeedEntityDao
import world.respect.datalayer.db.school.opds.daos.OpdsFeedMetadataEntityDao
import world.respect.datalayer.db.school.opds.daos.OpdsGroupEntityDao
import world.respect.datalayer.db.school.opds.daos.OpdsPublicationEntityDao
import world.respect.datalayer.db.school.opds.daos.PersonPasskeyEntityDao
import world.respect.datalayer.db.school.opds.daos.ReadiumLinkEntityDao
import world.respect.datalayer.db.school.opds.daos.ReadiumSubjectEntityDao
import world.respect.datalayer.db.school.opds.entities.OpdsFacetEntity
import world.respect.datalayer.db.school.opds.entities.OpdsFeedEntity
import world.respect.datalayer.db.school.opds.entities.OpdsFeedMetadataEntity
import world.respect.datalayer.db.school.opds.entities.OpdsGroupEntity
import world.respect.datalayer.db.school.opds.entities.OpdsPublicationEntity
import world.respect.datalayer.db.school.opds.entities.ReadiumLinkEntity
import world.respect.datalayer.db.school.opds.entities.ReadiumSubjectEntity
import world.respect.datalayer.db.school.SchoolTypeConverters
import world.respect.datalayer.db.school.daos.AuthTokenEntityDao
import world.respect.datalayer.db.school.daos.PersonEntityDao
import world.respect.datalayer.db.school.daos.PersonPasswordEntityDao
import world.respect.datalayer.db.school.daos.PersonRoleEntityDao
import world.respect.datalayer.db.school.entities.AuthTokenEntity
import world.respect.datalayer.db.school.entities.PersonEntity
import world.respect.datalayer.db.school.entities.PersonPasswordEntity
import world.respect.datalayer.db.school.entities.PersonRoleEntity
import world.respect.datalayer.db.shared.SharedConverters
import world.respect.datalayer.db.shared.daos.LangMapEntityDao
import world.respect.datalayer.db.shared.entities.LangMapEntity
import world.respect.datalayer.db.school.daos.IndicatorEntityDao
import world.respect.datalayer.db.school.daos.ReportEntityDao
import world.respect.datalayer.db.realm.entities.IndicatorEntity
import world.respect.datalayer.db.school.daos.ClassEntityDao
import world.respect.datalayer.db.school.daos.ClassPermissionEntityDao
import world.respect.datalayer.db.school.daos.EnrollmentEntityDao
import world.respect.datalayer.db.school.daos.InviteEntityDao
import world.respect.datalayer.db.school.daos.PersonQrBadgeEntityDao
import world.respect.datalayer.db.school.daos.PersonRelatedPersonEntityDao
import world.respect.datalayer.db.school.daos.PullSyncStatusEntityDao
import world.respect.datalayer.db.school.daos.WriteQueueItemEntityDao
import world.respect.datalayer.db.school.entities.ClassEntity
import world.respect.datalayer.db.school.entities.EnrollmentEntity
import world.respect.datalayer.db.school.entities.PersonQrBadgeEntity
import world.respect.datalayer.db.school.entities.InviteEntity
import world.respect.datalayer.db.school.entities.PersonPasskeyEntity
import world.respect.datalayer.db.school.entities.PersonRelatedPersonEntity
import world.respect.datalayer.db.school.entities.ReportEntity
import world.respect.datalayer.db.school.entities.WriteQueueItemEntity
import world.respect.datalayer.db.school.daos.SchoolPermissionGrantDao
import world.respect.datalayer.db.school.entities.ClassPermissionEntity
import world.respect.datalayer.db.school.entities.PullSyncStatusEntity
import world.respect.datalayer.db.school.entities.SchoolPermissionGrantEntity
import world.respect.datalayer.db.school.xapi.daos.XapiActivityEntityDao
import world.respect.datalayer.db.school.xapi.daos.XapiActivityExtensionDao
import world.respect.datalayer.db.school.xapi.daos.XapiActivityInteractionDao
import world.respect.datalayer.db.school.xapi.daos.XapiActivityProfileDocumentDao
import world.respect.datalayer.db.school.xapi.daos.XapiActivityProfileDocumentShaDao
import world.respect.datalayer.db.school.xapi.daos.XapiAgentProfileDocumentDao
import world.respect.datalayer.db.school.xapi.daos.XapiAgentProfileDocumentShaDao
import world.respect.datalayer.db.school.xapi.daos.XapiStateDocumentDao
import world.respect.datalayer.db.school.xapi.daos.XapiStateDocumentShaDao
import world.respect.datalayer.db.school.xapi.daos.XapiActorDao
import world.respect.datalayer.db.school.xapi.daos.XapiGroupMemberActorJoinDao
import world.respect.datalayer.db.school.xapi.daos.XapiRemoteWriteQueueItemEntityDao
import world.respect.datalayer.db.school.xapi.daos.XapiSessionEntityDao
import world.respect.datalayer.db.school.xapi.daos.XapiStatementContextActivityJoinDao
import world.respect.datalayer.db.school.xapi.daos.XapiStatementEntityDao
import world.respect.datalayer.db.school.xapi.daos.XapiStatementEntityJsonDao
import world.respect.datalayer.db.school.xapi.daos.XapiVerbDao
import world.respect.datalayer.db.school.xapi.daos.XapiVerbLangMapEntryDao
import world.respect.datalayer.db.school.xapi.entities.XapiActivityEntity
import world.respect.datalayer.db.school.xapi.entities.XapiActivityExtensionEntity
import world.respect.datalayer.db.school.xapi.entities.XapiActivityInteractionEntity
import world.respect.datalayer.db.school.xapi.entities.XapiActivityLangMapEntry
import world.respect.datalayer.db.school.xapi.entities.XapiActivityProfileDocumentEntity
import world.respect.datalayer.db.school.xapi.entities.XapiActivityProfileDocumentShaEntity
import world.respect.datalayer.db.school.xapi.entities.XapiAgentProfileDocumentEntity
import world.respect.datalayer.db.school.xapi.entities.XapiAgentProfileDocumentShaEntity
import world.respect.datalayer.db.school.xapi.entities.XapiStateDocumentEntity
import world.respect.datalayer.db.school.xapi.entities.XapiStateDocumentShaEntity
import world.respect.datalayer.db.school.xapi.entities.XapiActorEntity
import world.respect.datalayer.db.school.xapi.entities.XapiGroupMemberActorJoin
import world.respect.datalayer.db.school.xapi.entities.XapiRemoteWriteQueueItemEntity
import world.respect.datalayer.db.school.xapi.entities.XapiSessionEntity
import world.respect.datalayer.db.school.xapi.entities.XapiStatementContextActivityJoin
import world.respect.datalayer.db.school.xapi.entities.XapiStatementEntity
import world.respect.datalayer.db.school.xapi.entities.XapiStatementEntityJson
import world.respect.datalayer.db.school.xapi.entities.XapiVerbEntity
import world.respect.datalayer.db.school.xapi.entities.XapiVerbLangMapEntry
import world.respect.datalayer.school.model.Clazz
import world.respect.datalayer.school.model.Enrollment
import world.respect.lib.xapi.extensions.reportoptions.Indicator
import world.respect.datalayer.school.model.Invite2
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.Report


/**
 * Contains realm-specific entities and DAOs
 */
@Database(
    entities = [
        PersonEntity::class,
        PersonRoleEntity::class,
        PersonRelatedPersonEntity::class,
        PersonPasswordEntity::class,
        PersonPasskeyEntity::class,
        AuthTokenEntity::class,
        ReportEntity::class,
        IndicatorEntity::class,
        ClassEntity::class,
        ClassPermissionEntity::class,
        EnrollmentEntity::class,
        WriteQueueItemEntity::class,
        SchoolPermissionGrantEntity::class,
        PullSyncStatusEntity::class,
        PersonQrBadgeEntity::class,
        InviteEntity::class,

        //Shared (used by OPDS)
        LangMapEntity::class,

        //OPDS
        ReadiumLinkEntity::class,
        OpdsPublicationEntity::class,
        ReadiumSubjectEntity::class,
        OpdsFacetEntity::class,
        OpdsGroupEntity::class,
        OpdsFeedEntity::class,
        OpdsFeedMetadataEntity::class,

        //xAPI
        XapiActivityEntity::class,
        XapiActivityExtensionEntity::class,
        XapiActivityInteractionEntity::class,
        XapiActivityLangMapEntry::class,
        XapiActorEntity::class,
        XapiGroupMemberActorJoin::class,
        XapiStatementContextActivityJoin::class,
        XapiStatementEntity::class,
        XapiStatementEntityJson::class,
        XapiVerbEntity::class,
        XapiVerbLangMapEntry::class,
        XapiSessionEntity::class,
        XapiActivityProfileDocumentEntity::class,
        XapiActivityProfileDocumentShaEntity::class,
        XapiAgentProfileDocumentEntity::class,
        XapiAgentProfileDocumentShaEntity::class,
        XapiStateDocumentEntity::class,
        XapiStateDocumentShaEntity::class,
        XapiRemoteWriteQueueItemEntity::class,
    ],
    version = 20,
)
@TypeConverters(SharedConverters::class, SchoolTypeConverters::class, OpdsTypeConverters::class)
@ConstructedBy(RespectSchoolDatabaseConstructor::class)
abstract class RespectSchoolDatabase: RoomDatabase() {

    abstract fun getPersonEntityDao(): PersonEntityDao

    abstract fun getPersonPasswordEntityDao(): PersonPasswordEntityDao

    abstract fun getPersonQrBadgeEntityDao(): PersonQrBadgeEntityDao

    abstract fun getPersonPasskeyEntityDao(): PersonPasskeyEntityDao

    abstract fun getAuthTokenEntityDao(): AuthTokenEntityDao

    abstract fun getPersonRoleEntityDao(): PersonRoleEntityDao

    abstract fun getPersonRelatedPersonEntityDao(): PersonRelatedPersonEntityDao

    abstract fun getReportEntityDao(): ReportEntityDao

    abstract fun getIndicatorEntityDao(): IndicatorEntityDao

    abstract fun getClassEntityDao(): ClassEntityDao

    abstract fun getClassPermissionEntityDao(): ClassPermissionEntityDao

    abstract fun getEnrollmentEntityDao(): EnrollmentEntityDao

    abstract fun getWriteQueueItemEntityDao(): WriteQueueItemEntityDao

    abstract fun getInviteEntityDao(): InviteEntityDao

    abstract fun getSchoolPermissionGrantDao(): SchoolPermissionGrantDao

    abstract fun getPullSyncStatusEntityDao(): PullSyncStatusEntityDao

    abstract fun getLangMapEntityDao(): LangMapEntityDao

    abstract fun getOpdsFeedEntityDao(): OpdsFeedEntityDao

    abstract fun getOpdsPublicationEntityDao(): OpdsPublicationEntityDao

    abstract fun getOpdsFeedMetadataEntityDao(): OpdsFeedMetadataEntityDao

    abstract fun getReadiumLinkEntityDao(): ReadiumLinkEntityDao

    abstract fun getReadiumSubjectEntityDao(): ReadiumSubjectEntityDao

    abstract fun getOpdsGroupEntityDao(): OpdsGroupEntityDao

    abstract fun getActivityEntityDao(): XapiActivityEntityDao

    abstract fun getActivityExtensionDao(): XapiActivityExtensionDao

    abstract fun getActivityInteractionDao(): XapiActivityInteractionDao

    abstract fun getActivityLangMapEntryDao(): XapiActivityLangMapEntryDao

    abstract fun getStatementContextActivityJoinDao(): XapiStatementContextActivityJoinDao

    abstract fun getStatementDao(): XapiStatementEntityDao

    abstract fun getStatementEntityJsonDao(): XapiStatementEntityJsonDao

    abstract fun getActorDao(): XapiActorDao

    abstract fun getGroupMemberActorJoinDao(): XapiGroupMemberActorJoinDao

    abstract fun getVerbDao(): XapiVerbDao

    abstract fun getVerbLangMapEntryDao(): XapiVerbLangMapEntryDao

    abstract fun getXapiSessionEntityDao(): XapiSessionEntityDao

    abstract fun getActivityProfileDocumentDao(): XapiActivityProfileDocumentDao

    abstract fun getActivityProfileDocumentShaDao(): XapiActivityProfileDocumentShaDao

    abstract fun getAgentProfileDocumentDao(): XapiAgentProfileDocumentDao

    abstract fun getAgentProfileDocumentShaDao(): XapiAgentProfileDocumentShaDao

    abstract fun getStateDocumentDao(): XapiStateDocumentDao

    abstract fun getStateDocumentShaDao(): XapiStateDocumentShaDao

    abstract fun getXapiRemoteWriteQueueItemEntityDao(): XapiRemoteWriteQueueItemEntityDao

    companion object {

        val TABLE_IDS = listOf(
            Person.TABLE_ID,
            Report.TABLE_ID,
            Indicator.TABLE_ID,
            Enrollment.TABLE_ID,
            Clazz.TABLE_ID,
            PersonPasskeyEntity.TABLE_ID,
            Invite2.TABLE_ID,
            ReadiumLinkEntity.TABLE_ID,
            OpdsPublicationEntity.TABLE_ID,
            OpdsFacetEntity.TABLE_ID,
            OpdsGroupEntity.TABLE_ID,
            OpdsFeedEntity.TABLE_ID,
            ReadiumSubjectEntity.TABLE_ID,
        )

    }
}

// The Room compiler generates the `actual` implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING",
    "KotlinNoActualForExpect", "RedundantSuppression"
)
expect object RespectSchoolDatabaseConstructor : RoomDatabaseConstructor<RespectSchoolDatabase> {
    override fun initialize(): RespectSchoolDatabase
}