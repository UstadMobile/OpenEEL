package org.openeel.datalayer.db

import io.ktor.http.Url
import kotlinx.serialization.json.Json
import org.openeel.datalayer.AuthenticatedUserPrincipalId
import org.openeel.datalayer.SchoolDataSourceLocal
import org.openeel.datalayer.UidNumberMapper
import org.openeel.datalayer.db.school.ClassDatasourceDb
import org.openeel.datalayer.db.school.EnrollmentDataSourceDb
import org.openeel.datalayer.db.school.GetAuthenticatedPersonUseCase
import org.openeel.datalayer.db.school.IndicatorDataSourceDb
import org.openeel.datalayer.db.school.InviteDataSourceDb
import org.openeel.datalayer.db.school.PersonDataSourceDb
import org.openeel.datalayer.db.school.PersonPasskeyDataSourceDb
import org.openeel.datalayer.db.school.PersonPasswordDataSourceDb
import org.openeel.datalayer.db.school.PersonQrBadgeDataSourceDb
import org.openeel.datalayer.db.school.ReportDataSourceDb
import org.openeel.datalayer.db.school.SchoolPermissionGrantDataSourceDb
import org.openeel.datalayer.db.school.opds.OpdsFeedDataSourceDb
import org.openeel.datalayer.db.school.opds.OpdsPublicationDataSourceDb
import org.openeel.datalayer.db.school.xapi.XapiResourceDb
import org.openeel.datalayer.school.ClassDataSourceLocal
import org.openeel.datalayer.school.DummySchoolConfigSettingsDataSource
import org.openeel.datalayer.school.EnrollmentDataSourceLocal
import org.openeel.datalayer.school.IndicatorDataSource
import org.openeel.datalayer.school.InviteDataSourceLocal
import org.openeel.datalayer.school.PersonDataSourceLocal
import org.openeel.datalayer.school.PersonPasskeyDataSourceLocal
import org.openeel.datalayer.school.PersonPasswordDataSourceLocal
import org.openeel.datalayer.school.PersonQrCodeBadgeDataSourceLocal
import org.openeel.datalayer.school.ReportDataSourceLocal
import org.openeel.datalayer.school.SchoolConfigSettingDataSource
import org.openeel.datalayer.school.SchoolPermissionGrantDataSourceLocal
import org.openeel.datalayer.school.domain.CheckPersonPermissionUseCase
import org.openeel.datalayer.school.opds.OpdsFeedDataSourceLocal
import org.openeel.datalayer.school.opds.OpdsPublicationDataSourceLocal
import org.openeel.lib.xapi.resources.local.XapiResourceLocal
import org.openeel.lib.primarykeygen.PrimaryKeyGenerator

/**
 * SchoolDataSource implementation based on a local (Room) database
 *
 * @property schoolDb the school database
 * @property uidNumberMapper string uid to number mapper
 * @property authenticatedUser the authenticated user. The DataSource will use this to carry out
 *           permission checks as required, except when using putLocal functions (which are used by
 *           the repository to cache data from upstream).
 * @property schoolUrl the schoolUrl used by the Xapi datasource when creating actor objects.
 */
class SchoolDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
    private val checkPersonPermissionUseCase: CheckPersonPermissionUseCase,
    private val json: Json,
    private val defaultAppCatalogUrl: String?,
    private val primaryKeyGenerator: PrimaryKeyGenerator = PrimaryKeyGenerator(RespectSchoolDatabase.TABLE_IDS),
    private val schoolUrl: Url,
) : SchoolDataSourceLocal {

    private val getAuthenticatedPersonUseCase by lazy {
        GetAuthenticatedPersonUseCase(
            authenticatedUser, schoolDb, uidNumberMapper
        )
    }

    override val schoolPermissionGrantDataSource: SchoolPermissionGrantDataSourceLocal by lazy {
        SchoolPermissionGrantDataSourceDb(
            schoolPermissionGrantDao = schoolDb.getSchoolPermissionGrantDao(),
            uidNumberMapper = uidNumberMapper,
            authenticatedUser = authenticatedUser,
            getAuthenticatedPersonUseCase = getAuthenticatedPersonUseCase
        )
    }

    override val personDataSource: PersonDataSourceLocal by lazy {
        PersonDataSourceDb(schoolDb, uidNumberMapper, authenticatedUser, checkPersonPermissionUseCase)
    }

    override val personPasskeyDataSource: PersonPasskeyDataSourceLocal by lazy {
        PersonPasskeyDataSourceDb(schoolDb, uidNumberMapper, authenticatedUser)
    }

    override val personPasswordDataSource: PersonPasswordDataSourceLocal by lazy {
        PersonPasswordDataSourceDb(schoolDb, uidNumberMapper, checkPersonPermissionUseCase, authenticatedUser)
    }


    override val personQrBadgeDataSource: PersonQrCodeBadgeDataSourceLocal by lazy {
        PersonQrBadgeDataSourceDb(schoolDb, uidNumberMapper, authenticatedUser, checkPersonPermissionUseCase)
    }

    override val reportDataSource: ReportDataSourceLocal by lazy {
        ReportDataSourceDb(schoolDb)
    }

    override val indicatorDataSource: IndicatorDataSource by lazy {
        IndicatorDataSourceDb(schoolDb)
    }

    override val classDataSource: ClassDataSourceLocal by lazy {
        ClassDatasourceDb(schoolDb, uidNumberMapper, authenticatedUser)
    }

    override val inviteDataSource: InviteDataSourceLocal by lazy {
        InviteDataSourceDb(schoolDb, uidNumberMapper, checkPersonPermissionUseCase, authenticatedUser)
    }

    override val enrollmentDataSource: EnrollmentDataSourceLocal by lazy {
        EnrollmentDataSourceDb(schoolDb, uidNumberMapper, authenticatedUser)
    }

    override val opdsPublicationDataSource: OpdsPublicationDataSourceLocal by lazy {
        OpdsPublicationDataSourceDb(
            respectSchoolDatabase = schoolDb,
            json = json,
            uidNumberMapper = uidNumberMapper,
            primaryKeyGenerator = primaryKeyGenerator,
        )
    }

    override val opdsFeedDataSource: OpdsFeedDataSourceLocal by lazy {
        OpdsFeedDataSourceDb(
            schoolDb = schoolDb,
            uidNumberMapper = uidNumberMapper,
            authenticatedUser = authenticatedUser,
            json = json,
            primaryKeyGenerator = primaryKeyGenerator,
        )
    }

    override val schoolConfigSettingDataSource: SchoolConfigSettingDataSource by lazy {
        DummySchoolConfigSettingsDataSource(
            defaultAppCatalogUrl = defaultAppCatalogUrl,
        )
    }

    override val xapiResource: XapiResourceLocal by lazy {
        XapiResourceDb(
            schoolDb = schoolDb,
            uidNumberMapper = uidNumberMapper,
            authenticatedUser = authenticatedUser,
            checkPersonPermissionUseCase = checkPersonPermissionUseCase,
            json = json,
            schoolUrl = schoolUrl,
        )
    }

}