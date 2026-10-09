package world.respect.datalayer.school.domain

import kotlin.time.Instant

interface GetPermissionLastModifiedUseCase {

    suspend operator fun invoke() : Instant

}
