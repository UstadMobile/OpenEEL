package org.openeel.datalayer.db.school.domain

import org.openeel.datalayer.AuthenticatedUserPrincipalId
import org.openeel.datalayer.UidNumberMapper
import org.openeel.datalayer.db.RespectSchoolDatabase
import org.openeel.datalayer.school.domain.GetPermissionLastModifiedUseCase
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
