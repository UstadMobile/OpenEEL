package world.respect.datalayer.school.domain

import world.respect.datalayer.school.model.PersonRoleEnum

class GetWritableRolesListUseCaseImpl: GetWritableRolesListUseCase {

    override suspend fun invoke(currentPersonRole: PersonRoleEnum): List<PersonRoleEnum> {
        return when (currentPersonRole) {
            PersonRoleEnum.TEACHER -> listOf(
                PersonRoleEnum.STUDENT,
                PersonRoleEnum.PARENT,
            )
            PersonRoleEnum.SITE_ADMINISTRATOR, PersonRoleEnum.SYSTEM_ADMINISTRATOR -> listOf(
                PersonRoleEnum.STUDENT,
                PersonRoleEnum.PARENT,
                PersonRoleEnum.TEACHER,
                PersonRoleEnum.SYSTEM_ADMINISTRATOR,
            )
            else -> emptyList()
        }
    }

}