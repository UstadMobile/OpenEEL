package world.respect.datalayer.repository

import kotlinx.serialization.json.Json
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.repository.opds.OpdsFeedDataSourceRepository
import world.respect.datalayer.repository.opds.OpdsPublicationDataSourceRepository
import world.respect.datalayer.repository.school.ClassDataSourceRepository
import world.respect.datalayer.repository.school.EnrollmentDataSourceRepository
import world.respect.datalayer.repository.school.InviteDataSourceRepository
import world.respect.datalayer.repository.school.PersonDataSourceRepository
import world.respect.datalayer.repository.school.PersonPasskeyDataSourceRepository
import world.respect.datalayer.repository.school.PersonPasswordDataSourceRepository
import world.respect.datalayer.repository.school.PersonQrCodeBadgeDataSourceRepository
import world.respect.datalayer.repository.school.SchoolPermissionGrantDataSourceRepository
import world.respect.datalayer.repository.school.xapi.XapiResourceRepository
import world.respect.datalayer.school.IndicatorDataSource
import world.respect.datalayer.school.PersonPasskeyDataSource
import world.respect.datalayer.school.ReportDataSource
import world.respect.datalayer.school.opds.OpdsPublicationDataSource
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.lib.xapi.remotewritequeue.XapiRemoteWriteQueue
import world.respect.lib.xapi.resources.XapiResource

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
}