package org.openeel.datalayer

import org.openeel.datalayer.school.opds.OpdsPublicationDataSourceLocal
import org.openeel.datalayer.school.ClassDataSourceLocal
import org.openeel.datalayer.school.EnrollmentDataSourceLocal
import org.openeel.datalayer.school.InviteDataSourceLocal
import org.openeel.datalayer.school.PersonDataSourceLocal
import org.openeel.datalayer.school.PersonPasskeyDataSourceLocal
import org.openeel.datalayer.school.PersonPasswordDataSourceLocal
import org.openeel.datalayer.school.PersonQrCodeBadgeDataSourceLocal
import org.openeel.datalayer.school.ReportDataSourceLocal
import org.openeel.datalayer.school.SchoolPermissionGrantDataSourceLocal
import org.openeel.datalayer.school.opds.OpdsFeedDataSourceLocal
import org.openeel.lib.xapi.resources.local.XapiResourceLocal

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