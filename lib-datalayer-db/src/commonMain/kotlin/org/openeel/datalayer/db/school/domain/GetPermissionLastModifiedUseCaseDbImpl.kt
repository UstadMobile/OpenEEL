package world.respect.datalayer.db.school.domain

import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.school.domain.GetPermissionLastModifiedUseCase
import kotlin.time.Instant

class GetPermissionLastModifiedUseCaseDbImpl(
    private val schoolDb: RespectSchoolDatabase,
    private val numberMapper: UidNumberMapper,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
) : GetPermissionLastModifiedUseCase {

    override suspend fun invoke(): Instant {
        return Instant.fromEpochMilliseconds(
            schoolDb.getPersonEntityDao().getMostRecentPermissionChangeTime(
                numberMapper(authenticatedUser.guid)
            )
        )
    }
}
