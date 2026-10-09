package world.respect.datalayer

import world.respect.datalayer.school.opds.OpdsPublicationDataSourceLocal
import world.respect.datalayer.school.ClassDataSourceLocal
import world.respect.datalayer.school.EnrollmentDataSourceLocal
import world.respect.datalayer.school.InviteDataSourceLocal
import world.respect.datalayer.school.PersonDataSourceLocal
import world.respect.datalayer.school.PersonPasskeyDataSourceLocal
import world.respect.datalayer.school.PersonPasswordDataSourceLocal
import world.respect.datalayer.school.PersonQrCodeBadgeDataSourceLocal
import world.respect.datalayer.school.ReportDataSourceLocal
import world.respect.datalayer.school.SchoolPermissionGrantDataSourceLocal
import world.respect.datalayer.school.opds.OpdsFeedDataSourceLocal
import world.respect.lib.xapi.resources.local.XapiResourceLocal

/**
 * Local DataSource implementation (eg based on a database). Local DataSources include putLocal
 * functions which are used to insert data loaded from a trusted upstream server without permission
 * checks (to run an offline-first cache).
 */
interface SchoolDataSourceLocal: SchoolDataSource {

    override val schoolPermissionGrantDataSource: SchoolPermissionGrantDataSourceLocal

    override val personDataSource: PersonDataSourceLocal

    override val personPasskeyDataSource: PersonPasskeyDataSourceLocal

    override val personPasswordDataSource: PersonPasswordDataSourceLocal

    override val reportDataSource: ReportDataSourceLocal

    override val classDataSource: ClassDataSourceLocal

    override val personQrBadgeDataSource: PersonQrCodeBadgeDataSourceLocal

    override val enrollmentDataSource: EnrollmentDataSourceLocal

    override val inviteDataSource: InviteDataSourceLocal

    override val opdsPublicationDataSource: OpdsPublicationDataSourceLocal

    override val opdsFeedDataSource: OpdsFeedDataSourceLocal

    override val xapiResource: XapiResourceLocal

}