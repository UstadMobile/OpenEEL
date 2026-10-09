package org.openeel.datalayer

import org.openeel.datalayer.school.opds.OpdsPublicationDataSource
import org.openeel.datalayer.school.ClassDataSource
import org.openeel.datalayer.school.EnrollmentDataSource
import org.openeel.datalayer.school.ReportDataSource
import org.openeel.datalayer.school.IndicatorDataSource
import org.openeel.datalayer.school.InviteDataSource
import org.openeel.datalayer.school.PersonDataSource
import org.openeel.datalayer.school.PersonPasskeyDataSource
import org.openeel.datalayer.school.PersonPasswordDataSource
import org.openeel.datalayer.school.PersonQrBadgeDataSource
import org.openeel.datalayer.school.SchoolPermissionGrantDataSource
import org.openeel.datalayer.school.opds.OpdsFeedDataSource
import org.openeel.lib.xapi.resources.XapiResource

/**
 * DataSource for data which is specific to a given School and authenticated user (see
 * ARCHITECTURE.md for more info).
 *
 * The DataSource requires a user guid and (for a network client) an authorization token.
 */
interface SchoolDataSource {

    val schoolPermissionGrantDataSource: SchoolPermissionGrantDataSource

    val personDataSource: PersonDataSource

    val personPasskeyDataSource: PersonPasskeyDataSource

    val personPasswordDataSource: PersonPasswordDataSource

    val personQrBadgeDataSource: PersonQrBadgeDataSource

    val reportDataSource: ReportDataSource

    val indicatorDataSource: IndicatorDataSource

    val classDataSource: ClassDataSource

    val enrollmentDataSource: EnrollmentDataSource

    val inviteDataSource: InviteDataSource

    val opdsPublicationDataSource: OpdsPublicationDataSource

    val opdsFeedDataSource: OpdsFeedDataSource

    val xapiResource: XapiResource

}