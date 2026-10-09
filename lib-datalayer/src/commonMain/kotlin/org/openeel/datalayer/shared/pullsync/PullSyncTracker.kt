package world.respect.datalayer.shared.pullsync

import world.respect.datalayer.school.model.PullSyncStatus

/**
 * The PullSync runs periodically and/or on-demand to check for new data as required. A lot of user
 * data (e.g. enrollments, family members, etc) are very small and it makes sense to regularly pull
 * this data from the server and keep it ready for offline use.
 *
 * The procedure is designed to avoid:
 * a) Wasting bandwidth by repeatedly downloading the same data
 * b) Missing an update
 *
 * The general procedure is:
 * 1) Send a request for a given set of data
 * 2) The response includes a consistent-through timestamp. Subsequent requests set the since
 *    parameter ( GetListCommonParams.since ) to only download data that was stored since the last
 *    request using the consistent-through timestamp that was provided on the previous request.
 *
 * This is made more complicated by the fact that permissions can change e.g. in the following
 * sequence:
 * 1) Teacher enrolls student A. Enrollment is stored
 * 2) Student B runs a pull sync, and the consistent-through timestamp is updated.
 * 3) Student B is enrolled and now has permission to see Student A, but because the enrollment was
 *    stored before the consistent-through header, this data would be omitted.
 *
 * The above is avoided by making the since parameter conditional on when permissions last changed.
 * The HTTP response used for a PullSync MUST contain the Permissions-Last-Modified header.
 * Subsequent requests then include the sinceIfPermissionsNotChangedSince alongside the since
 * parameter. If permissions changed, the since parameter will be disregarded.
 *
 */
interface PullSyncTracker {

    suspend fun getPullSyncStatus(
        tableId: Int,
    ): PullSyncStatus?

    suspend fun updatePullSyncStatus(
        status: PullSyncStatus,
    )

}