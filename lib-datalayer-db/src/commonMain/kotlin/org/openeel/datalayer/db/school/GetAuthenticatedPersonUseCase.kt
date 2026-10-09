package world.respect.datalayer.db.school

import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.db.school.adapters.toPersonEntities
import world.respect.datalayer.school.model.Person

class GetAuthenticatedPersonUseCase(
    private val authenticatedUserPrincipalId: AuthenticatedUserPrincipalId,
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
) {

    suspend operator fun invoke(): Person? {
        return schoolDb.getPersonEntityDao().findByGuidNum(
            uidNumberMapper(authenticatedUserPrincipalId.guid)
        )?.toPersonEntities()?.toModel()
    }

}