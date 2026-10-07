package org.openeel.datalayer.repository

import kotlinx.serialization.json.Json
import org.openeel.datalayer.SchoolDataSource
import org.openeel.datalayer.SchoolDataSourceLocal
import org.openeel.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import org.openeel.datalayer.repository.opds.OpdsFeedDataSourceRepository
import org.openeel.datalayer.repository.opds.OpdsPublicationDataSourceRepository
import org.openeel.datalayer.repository.school.ClassDataSourceRepository
import org.openeel.datalayer.repository.school.EnrollmentDataSourceRepository
import org.openeel.datalayer.repository.school.InviteDataSourceRepository
import org.openeel.datalayer.repository.school.PersonDataSourceRepository
import org.openeel.datalayer.repository.school.PersonPasskeyDataSourceRepository
import org.openeel.datalayer.repository.school.PersonPasswordDataSourceRepository
import org.openeel.datalayer.repository.school.PersonQrCodeBadgeDataSourceRepository
import org.openeel.datalayer.repository.school.SchoolPermissionGrantDataSourceRepository
import org.openeel.datalayer.repository.school.xapi.XapiResourceRepository
import org.openeel.datalayer.school.IndicatorDataSource
import org.openeel.datalayer.school.PersonPasskeyDataSource
import org.openeel.datalayer.school.ReportDataSource
import org.openeel.datalayer.school.SchoolConfigSettingDataSource
import org.openeel.datalayer.school.opds.OpdsPublicationDataSource
import org.openeel.datalayer.school.writequeue.RemoteWriteQueue
import org.openeel.lib.xapi.remotewritequeue.XapiRemoteWriteQueue
import org.openeel.lib.xapi.resources.XapiResource

class SchoolDataSourceRepository(
    val local: SchoolDataSourceLocal,
    val remote: SchoolDataSource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
    private val remoteWriteQueue: RemoteWriteQueue,
    private val xapiRemoteWriteQueue: XapiRemoteWriteQueue,
    private val json: Json,
) : SchoolDataSource {

    override val reportDataSource: ReportDataSource by lazy {
        local.reportDataSource
    }

    override val indicatorDataSource: IndicatorDataSource by lazy {
        local.indicatorDataSource
    }

    override val schoolPermissionGrantDataSource: SchoolPermissionGrantDataSourceRepository by lazy {
        SchoolPermissionGrantDataSourceRepository(
            local = local.schoolPermissionGrantDataSource,
            remote = remote.schoolPermissionGrantDataSource,
            validationHelper = validationHelper,
            remoteWriteQueue = remoteWriteQueue,
        )
    }

    override val classDataSource: ClassDataSourceRepository by lazy {
        ClassDataSourceRepository(
            local = local.classDataSource,
            remote = remote.classDataSource,
            validationHelper = validationHelper,
            remoteWriteQueue = remoteWriteQueue,
        )
    }

    override val enrollmentDataSource: EnrollmentDataSourceRepository by lazy {
        EnrollmentDataSourceRepository(
            local = local.enrollmentDataSource,
            remote = remote.enrollmentDataSource,
            validationHelper = validationHelper,
            remoteWriteQueue = remoteWriteQueue,
        )
    }

    override val personDataSource: PersonDataSourceRepository by lazy {
        PersonDataSourceRepository(
            local.personDataSource,
            remote.personDataSource,
            validationHelper,
            remoteWriteQueue
        )
    }

    override val personPasswordDataSource: PersonPasswordDataSourceRepository by lazy {
        PersonPasswordDataSourceRepository(
            local = local.personPasswordDataSource,
            remote = remote.personPasswordDataSource,
            validationHelper = validationHelper,
            remoteWriteQueue = remoteWriteQueue,
        )
    }
    override val personQrBadgeDataSource: PersonQrCodeBadgeDataSourceRepository by lazy {
        PersonQrCodeBadgeDataSourceRepository(
            local = local.personQrBadgeDataSource,
            remote = remote.personQrBadgeDataSource,
            validationHelper = validationHelper,
            remoteWriteQueue = remoteWriteQueue,
        )
    }
    override val personPasskeyDataSource: PersonPasskeyDataSource by lazy {
        PersonPasskeyDataSourceRepository(
            local = local.personPasskeyDataSource,
            remote = remote.personPasskeyDataSource,
            validationHelper = validationHelper
        )
    }

    override val inviteDataSource: InviteDataSourceRepository by lazy {
        InviteDataSourceRepository(
            local = local.inviteDataSource,
            remote = remote.inviteDataSource,
            remoteWriteQueue = remoteWriteQueue,
            validationHelper = validationHelper
        )
    }

    override val opdsPublicationDataSource: OpdsPublicationDataSource by lazy {
        OpdsPublicationDataSourceRepository(
            local = local.opdsPublicationDataSource,
            remote = remote.opdsPublicationDataSource,
        )
    }

    override val opdsFeedDataSource: OpdsFeedDataSourceRepository by lazy {
        OpdsFeedDataSourceRepository(
            local = local.opdsFeedDataSource,
            remote = remote.opdsFeedDataSource,
        )
    }

    override val xapiResource: XapiResource by lazy {
        XapiResourceRepository(
            local = local.xapiResource,
            remote = remote.xapiResource,
            remoteWriteQueue = xapiRemoteWriteQueue,
            json = json,
        )
    }

    override val schoolConfigSettingDataSource: SchoolConfigSettingDataSource by lazy {
        local.schoolConfigSettingDataSource
    }
}