package org.openeel.server.account.invite

import org.koin.core.component.KoinComponent
import org.openeel.datalayer.UidNumberMapper
import org.openeel.datalayer.db.RespectSchoolDatabase
import org.openeel.datalayer.db.school.adapters.toModel
import org.openeel.datalayer.respect.model.invite.RespectInviteInfo
import org.openeel.lib.dataloadstate.throwable.withHttpStatus
import org.openeel.shared.domain.account.invite.GetInviteInfoUseCase

class GetInviteInfoUseCaseServer(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
) : GetInviteInfoUseCase, KoinComponent {

    override suspend fun invoke(code: String): RespectInviteInfo {

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

        return RespectInviteInfo(
            className = className,
            invite = invite.toModel(),
        )
    }
}