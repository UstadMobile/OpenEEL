package org.openeel.shared.domain.account.invite

import org.koin.core.component.KoinComponent
import org.openeel.datalayer.UidNumberMapper
import org.openeel.datalayer.db.RespectSchoolDatabase
import org.openeel.datalayer.db.school.adapters.toEntity
import org.openeel.datalayer.school.model.Invite2

/**
 * Used to create invites before datasource is available. This is invoked only on the server side
 * as part of setting up a school instance, hence no permission checks.
 */
class CreateInviteUseCaseDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
) : CreateInviteUseCase, KoinComponent {

    override suspend fun invoke(invite: Invite2) {
        schoolDb.getInviteEntityDao().insertAll(
            listOf(invite.toEntity(uidNumberMapper))
        )
    }
}
