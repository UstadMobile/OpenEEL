package org.openeel.datalayer.db.school

import org.openeel.datalayer.AuthenticatedUserPrincipalId
import org.openeel.datalayer.UidNumberMapper
import org.openeel.datalayer.db.SchoolDatabase
import org.openeel.datalayer.db.school.adapters.toModel
import org.openeel.datalayer.db.school.adapters.toPersonEntities
import org.openeel.datalayer.school.model.Person

class GetAuthenticatedPersonUseCase(
    private val authenticatedUserPrincipalId: AuthenticatedUserPrincipalId,
    private val schoolDb: SchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
) {

    suspend operator fun invoke(): Person? {
        return schoolDb.getPersonEntityDao().findByGuidNum(
            uidNumberMapper(authenticatedUserPrincipalId.guid)
        )?.toPersonEntities()?.toModel()
    }

}