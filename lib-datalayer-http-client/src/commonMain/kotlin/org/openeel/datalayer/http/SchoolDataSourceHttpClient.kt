package org.openeel.datalayer.http

import io.ktor.client.HttpClient
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import org.openeel.datalayer.AuthTokenProvider
import org.openeel.datalayer.SchoolDataSource
import org.openeel.datalayer.http.ext.schoolDirectoryEntryOrNull
import org.openeel.datalayer.http.school.ClassDataSourceHttpClient
import org.openeel.datalayer.http.school.EnrollmentDataSourceHttpClient
import org.openeel.datalayer.http.school.InviteDataSourceHttpClient
import org.openeel.datalayer.http.school.PersonDataSourceHttpClient
import org.openeel.datalayer.http.school.PersonPasskeyDataSourceHttpClient
import org.openeel.datalayer.http.school.PersonPasswordDataSourceHttpClient
import org.openeel.datalayer.http.school.PersonQrBadgeDataSourceHttpClient
import org.openeel.datalayer.http.school.SchoolPermissionGrantDataSourceHttpClient
import org.openeel.datalayer.http.school.opds.OpdsFeedDataSourceHttpClient
import org.openeel.datalayer.http.school.opds.OpdsPublicationDataSourceHttpClient
import org.openeel.datalayer.http.school.xapi.XapiResourceHttpClient
import org.openeel.datalayer.networkvalidation.BaseDataSourceValidationHelper
import org.openeel.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import org.openeel.datalayer.school.ClassDataSource
import org.openeel.datalayer.school.DummySchoolConfigSettingsDataSource
import org.openeel.datalayer.school.EnrollmentDataSource
import org.openeel.datalayer.school.IndicatorDataSource
import org.openeel.datalayer.school.InviteDataSource
import org.openeel.datalayer.school.PersonDataSource
import org.openeel.datalayer.school.PersonPasskeyDataSource
import org.openeel.datalayer.school.PersonPasswordDataSource
import org.openeel.datalayer.school.PersonQrBadgeDataSource
import org.openeel.datalayer.school.ReportDataSource
import org.openeel.datalayer.school.SchoolConfigSettingDataSource
import org.openeel.datalayer.school.SchoolPermissionGrantDataSource
import org.openeel.datalayer.school.opds.OpdsFeedDataSource
import org.openeel.datalayer.school.opds.OpdsPublicationDataSource
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryResource
import org.openeel.lib.xapi.resources.XapiResource

class SchoolDataSourceHttpClient(
    private val schoolUrl: Url,
    private val schoolDirectoryEntryResource: SchoolDirectoryEntryResource,
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
            schoolDirectoryEntryResource = schoolDirectoryEntryResource,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            validationHelper = validationHelper,
        )
    }

    override val personDataSource: PersonDataSource by lazy {
        PersonDataSourceHttpClient(
            schoolUrl = schoolUrl,
            schoolDirectoryEntryResource = schoolDirectoryEntryResource,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            validationHelper = validationHelper,
        )
    }

    override val personPasskeyDataSource: PersonPasskeyDataSource by lazy {
        PersonPasskeyDataSourceHttpClient(
            schoolUrl = schoolUrl,
            schoolDirectoryEntryResource = schoolDirectoryEntryResource,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            validationHelper = validationHelper,
        )
    }

    override val personPasswordDataSource: PersonPasswordDataSource by lazy {
        PersonPasswordDataSourceHttpClient(
            schoolUrl = schoolUrl,
            schoolDirectoryEntryResource = schoolDirectoryEntryResource,
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
            schoolDirectoryEntryResource = schoolDirectoryEntryResource,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            validationHelper = validationHelper,
        )
    }

    override val personQrBadgeDataSource: PersonQrBadgeDataSource by lazy {
        PersonQrBadgeDataSourceHttpClient(
            schoolUrl = schoolUrl,
            schoolDirectoryEntryResource = schoolDirectoryEntryResource,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            validationHelper = validationHelper,
        )
    }

    override val enrollmentDataSource: EnrollmentDataSource by lazy {
        EnrollmentDataSourceHttpClient(
            schoolUrl = schoolUrl,
            schoolDirectoryEntryResource = schoolDirectoryEntryResource,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            validationHelper = validationHelper,
        )
    }

    override val inviteDataSource: InviteDataSource by lazy {
        InviteDataSourceHttpClient(
            schoolUrl = schoolUrl,
            schoolDirectoryEntryResource = schoolDirectoryEntryResource,
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

    override val schoolConfigSettingDataSource: SchoolConfigSettingDataSource by lazy {
        DummySchoolConfigSettingsDataSource(
            defaultAppCatalogUrl = defaultAppCatalogUrl,
        )
    }

    override val xapiResource: XapiResource by lazy {
        XapiResourceHttpClient(
            xapiUrl = {
                schoolDirectoryEntryResource.schoolDirectoryEntryOrNull(schoolUrl)?.xapi
                    ?: throw IllegalStateException("SchoolUrl $schoolUrl has no XAPI URL")
            },
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            json = json,
        )
    }

}