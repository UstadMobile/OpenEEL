package org.openeel.datalayer.school.domain

import org.openeel.lib.directory.model.PersonRoleEnum

interface GetWritableRolesListUseCase {

    suspend operator fun invoke(
        currentPersonRole: PersonRoleEnum
    ): List<PersonRoleEnum>

}