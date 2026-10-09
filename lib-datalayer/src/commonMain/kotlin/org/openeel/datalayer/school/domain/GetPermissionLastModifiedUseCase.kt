package org.openeel.datalayer.school.domain

import kotlin.time.Instant

interface GetPermissionLastModifiedUseCase {

    suspend operator fun invoke() : Instant

}
