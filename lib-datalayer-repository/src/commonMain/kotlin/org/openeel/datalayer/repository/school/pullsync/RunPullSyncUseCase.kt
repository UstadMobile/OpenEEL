package world.respect.datalayer.repository.school.pullsync

import io.github.aakira.napier.Napier
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.dataloadstate.ext.isLoadedOrNotModified
import world.respect.datalayer.school.EnrollmentDataSource
import world.respect.datalayer.school.model.Enrollment
import world.respect.datalayer.school.model.PullSyncStatus
import world.respect.datalayer.shared.pullsync.PullSyncTracker
import world.respect.datalayer.shared.params.GetListCommonParams
import java.lang.IllegalStateException
import kotlin.time.Instant

/**
 * Handling permissions requires that all available enrollments for the authenticated user are kept
 * up to date: Class based permissions may be granted by role (e.g. to teachers of the class or to
 * teachers of the class).
 *
 * This is needed not only to determine which permissions are granted to the user, but also their
 * scope : eg. a student (and therefor their parent) is by default granted the PERSON_STUDENT_READ
 * permission for the class. This means they can view the names of students in their class. For the
 * permission query to work, it needs the EnrollmentEntity for both the authenticated user _and_ all
 * others in the class.
 *
 * Should probably be done using WorkManager...
 */
class RunPullSyncUseCase(
    private val pullSyncTracker: PullSyncTracker,
    private val schoolDataSource: SchoolDataSource,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
) {

    suspend operator fun invoke() {
        val tableId = Enrollment.TABLE_ID

        val status = pullSyncTracker.getPullSyncStatus(tableId) ?: PullSyncStatus(
            accountPersonUid = authenticatedUser.guid,
            consistentThrough = Instant.fromEpochMilliseconds(0),
            permissionsLastModified = Instant.fromEpochMilliseconds(0),
            tableId = tableId,
        )

        var since: Instant = status.consistentThrough
        var permissionsLastModified = status.permissionsLastModified

        do {
            Napier.d("PullSync: Loading since $since")
            val loadResult = schoolDataSource.enrollmentDataSource.list(
                loadParams = DataLoadParams(),
                listParams = EnrollmentDataSource.GetListParams(
                    common = GetListCommonParams(
                        since = since,
                        sinceIfPermissionsNotChangedSince = permissionsLastModified,
                    )
                )
            )


            val remoteDataLoadedState = loadResult.remoteState

            if(remoteDataLoadedState?.isLoadedOrNotModified() != true) {
                throw IllegalStateException("Could not load remote data")
            }

            val remoteData = remoteDataLoadedState.dataOrNull() as? List<*> ?: emptyList<Any>()

            since = loadResult.remoteState?.metaInfo?.consistentThrough ?: throw IllegalStateException(
                "RunPullSyncUseCase: Load result MUST have consistentThrough"
            )
            permissionsLastModified = loadResult.remoteState?.metaInfo?.permissionsLastModified ?: throw IllegalStateException(
                "RunPullSyncUseCase: Load result MUST have permissionsLastModified"
            )
        }while (remoteData.isNotEmpty())

        Napier.d("PullSync: Done consistent through to $since / permissions last modified $permissionsLastModified")
        pullSyncTracker.updatePullSyncStatus(
            status.copy(consistentThrough = since)
        )
    }

}