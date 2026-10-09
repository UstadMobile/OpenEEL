package world.respect.shared.domain.account.invite

import world.respect.datalayer.school.model.Invite2

/**
 * UseCase to create an invite when datasource is not yet available (eg system default invites for
 * new persons by role -student, teacher, parent, etc).
 */
interface CreateInviteUseCase {

    suspend operator fun invoke(invite: Invite2)

}