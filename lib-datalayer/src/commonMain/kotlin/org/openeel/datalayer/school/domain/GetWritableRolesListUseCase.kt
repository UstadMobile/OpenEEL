package world.respect.datalayer.school.domain

import world.respect.datalayer.school.model.PersonRoleEnum

interface GetWritableRolesListUseCase {

    suspend operator fun invoke(
        currentPersonRole: PersonRoleEnum
    ): List<PersonRoleEnum>

}