package org.openeel.server.account.invite

import org.koin.core.component.KoinComponent
import org.openeel.datalayer.UidNumberMapper
import org.openeel.datalayer.db.SchoolDatabase
import org.openeel.datalayer.db.school.adapters.toModel
import org.openeel.datalayer.respect.model.invite.InviteInfo
import org.openeel.lib.dataloadstate.throwable.withHttpStatus
import org.openeel.shared.domain.account.invite.GetInviteInfoUseCase

class GetInviteInfoUseCaseServer(
    private val schoolDb: SchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
) : GetInviteInfoUseCase, KoinComponent {

    override suspend fun invoke(code: String): InviteInfo {

        val invite = schoolDb.getInviteEntityDao().getInviteByInviteCode(code)
            ?: throw IllegalArgumentException("invite not found for code: $code")
                .withHttpStatus(404)

        val classUid = invite.iForClassGuid
        val className = if(classUid != null) {
            schoolDb.getClassEntityDao().findByGuid(uidNumberMapper(classUid))
                ?.clazz?.cTitle
        }else {
            null
        }

        return InviteInfo(
            className = className,
            invite = invite.toModel(),
        )
    }
}