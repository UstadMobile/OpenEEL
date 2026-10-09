package world.respect.datalayer.http

import io.ktor.client.HttpClient
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import world.respect.datalayer.AuthTokenProvider
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.http.ext.schoolDirectoryEntryOrNull
import world.respect.datalayer.http.school.ClassDataSourceHttpClient
import world.respect.datalayer.http.school.EnrollmentDataSourceHttpClient
import world.respect.datalayer.http.school.InviteDataSourceHttpClient
import world.respect.datalayer.http.school.PersonDataSourceHttpClient
import world.respect.datalayer.http.school.PersonPasskeyDataSourceHttpClient
import world.respect.datalayer.http.school.PersonPasswordDataSourceHttpClient
import world.respect.datalayer.http.school.PersonQrBadgeDataSourceHttpClient
import world.respect.datalayer.http.school.SchoolPermissionGrantDataSourceHttpClient
import world.respect.datalayer.http.school.opds.OpdsFeedDataSourceHttpClient
import world.respect.datalayer.http.school.opds.OpdsPublicationDataSourceHttpClient
import world.respect.datalayer.http.school.xapi.XapiResourceHttpClient
import world.respect.datalayer.networkvalidation.BaseDataSourceValidationHelper
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.school.ClassDataSource
import world.respect.datalayer.school.EnrollmentDataSource
import world.respect.datalayer.school.IndicatorDataSource
import world.respect.datalayer.school.InviteDataSource
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.PersonPasskeyDataSource
import world.respect.datalayer.school.PersonPasswordDataSource
import world.respect.datalayer.school.PersonQrBadgeDataSource
import world.respect.datalayer.school.ReportDataSource
import world.respect.datalayer.school.SchoolPermissionGrantDataSource
import world.respect.datalayer.school.opds.OpdsFeedDataSource
import world.respect.datalayer.school.opds.OpdsPublicationDataSource
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import world.respect.lib.xapi.resources.XapiResource

class SchoolDataSourceHttpClient(
    private val schoolUrl: Url,
    private val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
    private val validationHelper: ExtendedDataSourceValidationHelper,
    private val json: Json,
    private val defaultAppCatalogUrl: String?,
    private val opdsFeedValidationHelper: BaseDataSourceValidationHelper? = null,
    private val opdsPublicationValidationHelper: BaseDataSourceValidationHelper? = null,
) : SchoolDataSource {

    override val schoolPermissionGrantDataSource: SchoolPermissionGrantDataSource by lazy {
        SchoolPermissionGrantDataSourceHttpClient(
            schoolUrl = schoolUrl,
            schoolDirectoryEntryDataSource = schoolDirectoryEntryDataSource,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            validationHelper = validationHelper,
        )
    }

    override val personDataSource: PersonDataSource by lazy {
        PersonDataSourceHttpClient(
            schoolUrl = schoolUrl,
            schoolDirectoryEntryDataSource = schoolDirectoryEntryDataSource,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            validationHelper = validationHelper,
        )
    }

    override val personPasskeyDataSource: PersonPasskeyDataSource by lazy {
        PersonPasskeyDataSourceHttpClient(
            schoolUrl = schoolUrl,
            schoolDirectoryEntryDataSource = schoolDirectoryEntryDataSource,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            validationHelper = validationHelper,
        )
    }

    override val personPasswordDataSource: PersonPasswordDataSource by lazy {
        PersonPasswordDataSourceHttpClient(
            schoolUrl = schoolUrl,
            schoolDirectoryEntryDataSource = schoolDirectoryEntryDataSource,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            validationHelper = validationHelper,
        )
    }

    override val reportDataSource: ReportDataSource
        get() = TODO("Not yet implemented")

    override val indicatorDataSource: IndicatorDataSource
        get() = TODO("Not yet implemented")

    override val classDataSource: ClassDataSource by lazy {
        ClassDataSourceHttpClient(
            schoolUrl = schoolUrl,
            schoolDirectoryEntryDataSource = schoolDirectoryEntryDataSource,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            validationHelper = validationHelper,
        )
    }

    override val personQrBadgeDataSource: PersonQrBadgeDataSource by lazy {
        PersonQrBadgeDataSourceHttpClient(
            schoolUrl = schoolUrl,
            schoolDirectoryEntryDataSource = schoolDirectoryEntryDataSource,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            validationHelper = validationHelper,
        )
    }

    override val enrollmentDataSource: EnrollmentDataSource by lazy {
        EnrollmentDataSourceHttpClient(
            schoolUrl = schoolUrl,
            schoolDirectoryEntryDataSource = schoolDirectoryEntryDataSource,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            validationHelper = validationHelper,
        )
    }

    override val inviteDataSource: InviteDataSource by lazy {
        InviteDataSourceHttpClient(
            schoolUrl = schoolUrl,
            schoolDirectoryEntryDataSource = schoolDirectoryEntryDataSource,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            validationHelper = validationHelper,
        )
    }

    override val opdsPublicationDataSource: OpdsPublicationDataSource by lazy {
        OpdsPublicationDataSourceHttpClient(
            httpClient = httpClient,
            json = json,
            publicationValidationHelper =  opdsPublicationValidationHelper,
        )
    }

    override val opdsFeedDataSource: OpdsFeedDataSource by lazy {
        OpdsFeedDataSourceHttpClient(
            httpClient = httpClient,
        )
    }

    override val xapiResource: XapiResource by lazy {
        XapiResourceHttpClient(
            xapiUrl = {
                schoolDirectoryEntryDataSource.schoolDirectoryEntryOrNull(schoolUrl)?.xapi
                    ?: throw IllegalStateException("SchoolUrl $schoolUrl has no XAPI URL")
            },
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            json = json,
        )
    }

}