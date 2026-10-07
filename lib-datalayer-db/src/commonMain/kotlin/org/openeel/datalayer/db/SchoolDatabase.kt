package org.openeel.datalayer.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.ustadmobile.core.db.dao.xapi.XapiActivityLangMapEntryDao
import org.openeel.datalayer.db.school.opds.OpdsTypeConverters
import org.openeel.datalayer.db.school.opds.daos.OpdsFeedEntityDao
import org.openeel.datalayer.db.school.opds.daos.OpdsFeedMetadataEntityDao
import org.openeel.datalayer.db.school.opds.daos.OpdsGroupEntityDao
import org.openeel.datalayer.db.school.opds.daos.OpdsPublicationEntityDao
import org.openeel.datalayer.db.school.opds.daos.PersonPasskeyEntityDao
import org.openeel.datalayer.db.school.opds.daos.ReadiumLinkEntityDao
import org.openeel.datalayer.db.school.opds.daos.ReadiumSubjectEntityDao
import org.openeel.datalayer.db.school.opds.entities.OpdsFacetEntity
import org.openeel.datalayer.db.school.opds.entities.OpdsFeedEntity
import org.openeel.datalayer.db.school.opds.entities.OpdsFeedMetadataEntity
import org.openeel.datalayer.db.school.opds.entities.OpdsGroupEntity
import org.openeel.datalayer.db.school.opds.entities.OpdsPublicationEntity
import org.openeel.datalayer.db.school.opds.entities.ReadiumLinkEntity
import org.openeel.datalayer.db.school.opds.entities.ReadiumSubjectEntity
import org.openeel.datalayer.db.school.SchoolTypeConverters
import org.openeel.datalayer.db.school.daos.AuthTokenEntityDao
import org.openeel.datalayer.db.school.daos.PersonEntityDao
import org.openeel.datalayer.db.school.daos.PersonPasswordEntityDao
import org.openeel.datalayer.db.school.daos.PersonRoleEntityDao
import org.openeel.datalayer.db.school.entities.AuthTokenEntity
import org.openeel.datalayer.db.school.entities.PersonEntity
import org.openeel.datalayer.db.school.entities.PersonPasswordEntity
import org.openeel.datalayer.db.school.entities.PersonRoleEntity
import org.openeel.datalayer.db.shared.SharedConverters
import org.openeel.datalayer.db.shared.daos.LangMapEntityDao
import org.openeel.datalayer.db.shared.entities.LangMapEntity
import org.openeel.datalayer.db.school.daos.IndicatorEntityDao
import org.openeel.datalayer.db.school.daos.ReportEntityDao
import org.openeel.datalayer.db.realm.entities.IndicatorEntity
import org.openeel.datalayer.db.school.daos.ClassEntityDao
import org.openeel.datalayer.db.school.daos.ClassPermissionEntityDao
import org.openeel.datalayer.db.school.daos.EnrollmentEntityDao
import org.openeel.datalayer.db.school.daos.InviteEntityDao
import org.openeel.datalayer.db.school.daos.PersonQrBadgeEntityDao
import org.openeel.datalayer.db.school.daos.PersonRelatedPersonEntityDao
import org.openeel.datalayer.db.school.daos.PullSyncStatusEntityDao
import org.openeel.datalayer.db.school.daos.WriteQueueItemEntityDao
import org.openeel.datalayer.db.school.entities.ClassEntity
import org.openeel.datalayer.db.school.entities.EnrollmentEntity
import org.openeel.datalayer.db.school.entities.PersonQrBadgeEntity
import org.openeel.datalayer.db.school.entities.InviteEntity
import org.openeel.datalayer.db.school.entities.PersonPasskeyEntity
import org.openeel.datalayer.db.school.entities.PersonRelatedPersonEntity
import org.openeel.datalayer.db.school.entities.ReportEntity
import org.openeel.datalayer.db.school.entities.WriteQueueItemEntity
import org.openeel.datalayer.db.school.daos.SchoolPermissionGrantDao
import org.openeel.datalayer.db.school.entities.ClassPermissionEntity
import org.openeel.datalayer.db.school.entities.PullSyncStatusEntity
import org.openeel.datalayer.db.school.entities.SchoolPermissionGrantEntity
import org.openeel.datalayer.db.school.xapi.daos.XapiActivityEntityDao
import org.openeel.datalayer.db.school.xapi.daos.XapiActivityExtensionDao
import org.openeel.datalayer.db.school.xapi.daos.XapiActivityInteractionDao
import org.openeel.datalayer.db.school.xapi.daos.XapiActivityProfileDocumentDao
import org.openeel.datalayer.db.school.xapi.daos.XapiActivityProfileDocumentShaDao
import org.openeel.datalayer.db.school.xapi.daos.XapiAgentProfileDocumentDao
import org.openeel.datalayer.db.school.xapi.daos.XapiAgentProfileDocumentShaDao
import org.openeel.datalayer.db.school.xapi.daos.XapiStateDocumentDao
import org.openeel.datalayer.db.school.xapi.daos.XapiStateDocumentShaDao
import org.openeel.datalayer.db.school.xapi.daos.XapiActorDao
import org.openeel.datalayer.db.school.xapi.daos.XapiGroupMemberActorJoinDao
import org.openeel.datalayer.db.school.xapi.daos.XapiRemoteWriteQueueItemEntityDao
import org.openeel.datalayer.db.school.xapi.daos.XapiSessionEntityDao
import org.openeel.datalayer.db.school.xapi.daos.XapiStatementContextActivityJoinDao
import org.openeel.datalayer.db.school.xapi.daos.XapiStatementEntityDao
import org.openeel.datalayer.db.school.xapi.daos.XapiStatementEntityJsonDao
import org.openeel.datalayer.db.school.xapi.daos.XapiVerbDao
import org.openeel.datalayer.db.school.xapi.daos.XapiVerbLangMapEntryDao
import org.openeel.datalayer.db.school.xapi.entities.XapiActivityEntity
import org.openeel.datalayer.db.school.xapi.entities.XapiActivityExtensionEntity
import org.openeel.datalayer.db.school.xapi.entities.XapiActivityInteractionEntity
import org.openeel.datalayer.db.school.xapi.entities.XapiActivityLangMapEntry
import org.openeel.datalayer.db.school.xapi.entities.XapiActivityProfileDocumentEntity
import org.openeel.datalayer.db.school.xapi.entities.XapiActivityProfileDocumentShaEntity
import org.openeel.datalayer.db.school.xapi.entities.XapiAgentProfileDocumentEntity
import org.openeel.datalayer.db.school.xapi.entities.XapiAgentProfileDocumentShaEntity
import org.openeel.datalayer.db.school.xapi.entities.XapiStateDocumentEntity
import org.openeel.datalayer.db.school.xapi.entities.XapiStateDocumentShaEntity
import org.openeel.datalayer.db.school.xapi.entities.XapiActorEntity
import org.openeel.datalayer.db.school.xapi.entities.XapiGroupMemberActorJoin
import org.openeel.datalayer.db.school.xapi.entities.XapiRemoteWriteQueueItemEntity
import org.openeel.datalayer.db.school.xapi.entities.XapiSessionEntity
import org.openeel.datalayer.db.school.xapi.entities.XapiStatementContextActivityJoin
import org.openeel.datalayer.db.school.xapi.entities.XapiStatementEntity
import org.openeel.datalayer.db.school.xapi.entities.XapiStatementEntityJson
import org.openeel.datalayer.db.school.xapi.entities.XapiVerbEntity
import org.openeel.datalayer.db.school.xapi.entities.XapiVerbLangMapEntry
import org.openeel.datalayer.school.model.Clazz
import org.openeel.datalayer.school.model.Enrollment
import org.openeel.lib.xapi.extensions.reportoptions.Indicator
import org.openeel.datalayer.school.model.Invite2
import org.openeel.datalayer.school.model.Person
import org.openeel.datalayer.school.model.Report


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
    version = 19,
)
@TypeConverters(SharedConverters::class, SchoolTypeConverters::class, OpdsTypeConverters::class)
@ConstructedBy(RespectSchoolDatabaseConstructor::class)
abstract class SchoolDatabase: RoomDatabase() {

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
expect object RespectSchoolDatabaseConstructor : RoomDatabaseConstructor<SchoolDatabase> {
    override fun initialize(): SchoolDatabase
}