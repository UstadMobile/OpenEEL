package world.respect.datalayer.db

import io.ktor.http.Url
import kotlinx.serialization.json.Json
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.ClassDatasourceDb
import world.respect.datalayer.db.school.EnrollmentDataSourceDb
import world.respect.datalayer.db.school.GetAuthenticatedPersonUseCase
import world.respect.datalayer.db.school.GetAuthenticatedXapiAgentsUseCaseDbImpl
import world.respect.datalayer.db.school.IndicatorDataSourceDb
import world.respect.datalayer.db.school.InviteDataSourceDb
import world.respect.datalayer.db.school.PersonDataSourceDb
import world.respect.datalayer.db.school.PersonPasskeyDataSourceDb
import world.respect.datalayer.db.school.PersonPasswordDataSourceDb
import world.respect.datalayer.db.school.PersonQrBadgeDataSourceDb
import world.respect.datalayer.db.school.ReportDataSourceDb
import world.respect.datalayer.db.school.SchoolPermissionGrantDataSourceDb
import world.respect.datalayer.db.school.opds.OpdsFeedDataSourceDb
import world.respect.datalayer.db.school.opds.OpdsPublicationDataSourceDb
import world.respect.datalayer.db.school.xapi.XapiResourceDb
import world.respect.datalayer.school.ClassDataSourceLocal
import world.respect.datalayer.school.EnrollmentDataSourceLocal
import world.respect.datalayer.school.IndicatorDataSource
import world.respect.datalayer.school.InviteDataSourceLocal
import world.respect.datalayer.school.PersonDataSourceLocal
import world.respect.datalayer.school.PersonPasskeyDataSourceLocal
import world.respect.datalayer.school.PersonPasswordDataSourceLocal
import world.respect.datalayer.school.PersonQrCodeBadgeDataSourceLocal
import world.respect.datalayer.school.ReportDataSourceLocal
import world.respect.datalayer.school.SchoolPermissionGrantDataSourceLocal
import world.respect.datalayer.school.domain.CheckPersonPermissionUseCase
import world.respect.datalayer.school.opds.OpdsFeedDataSourceLocal
import world.respect.datalayer.school.opds.OpdsPublicationDataSourceLocal
import world.respect.lib.primarykeygen.PrimaryKeyGenerator
import world.respect.lib.xapi.auth.GetAuthenticatedXapiAgentsUseCase
import world.respect.lib.xapi.resources.local.XapiResourceLocal

/**
 * SchoolDataSource implementation based on a local (Room) database
 *
 * @property schoolDb the school database
 * @property uidNumberMapper string uid to number mapper
 * @property authenticatedUser the authenticated user. The DataSource will use this to carry out
 *           permission checks as required, except when using putLocal functions (which are used by
 *           the repository to cache data from upstream).
 * @property schoolUrl the schoolUrl used by the Xapi datasource when creating actor objects.
 * @property getAuthenticatedPersonUseCase Implementation of [GetAuthenticatedXapiAgentsUseCase].
 *           This is normally GetAuthenticatedXapiAgentsUseCaseDbImpl but can be overridden for
 *           testing.
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
    authenticatedXapiAgentsUseCase: GetAuthenticatedXapiAgentsUseCase? = null,
) : SchoolDataSourceLocal {

    private val getAuthenticatedPersonUseCase = GetAuthenticatedPersonUseCase(
        authenticatedUser, schoolDb, uidNumberMapper
    )

    private val getAuthenticatedXapiAgentsUseCase = authenticatedXapiAgentsUseCase ?:
        GetAuthenticatedXapiAgentsUseCaseDbImpl(getAuthenticatedPersonUseCase, schoolUrl)

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

    override val xapiResource: XapiResourceLocal by lazy {
        XapiResourceDb(
            schoolDb = schoolDb,
            uidNumberMapper = uidNumberMapper,
            authenticatedUser = authenticatedUser,
            json = json,
            schoolUrl = schoolUrl,
            getAuthenticatedXapiAgentsUseCase = getAuthenticatedXapiAgentsUseCase,
        )
    }

}