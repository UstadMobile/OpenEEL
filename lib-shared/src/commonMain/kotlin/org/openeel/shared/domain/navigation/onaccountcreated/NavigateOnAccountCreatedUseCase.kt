package world.respect.shared.domain.navigation.onaccountcreated

import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableSharedFlow
import world.respect.datalayer.school.model.ClassInvite
import world.respect.datalayer.school.model.ClassInviteModeEnum
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonStatusEnum
import world.respect.shared.domain.account.invite.RespectRedeemInviteRequest
import world.respect.shared.navigation.Home
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.SignupScreen
import world.respect.shared.navigation.WaitingForApproval
import world.respect.shared.viewmodel.manageuser.signup.SignupScreenModeEnum

/**
 * Decide where to navigate after a user account has been created.
 */
class NavigateOnAccountCreatedUseCase(
    private val schoolUrl: Url,
) {

    operator fun invoke(
        personRegistered: Person,
        navCommandFlow: MutableSharedFlow<NavCommand>,
        inviteRequest: RespectRedeemInviteRequest? = null,
    ) {
        val invite = inviteRequest?.invite

        navCommandFlow.tryEmit(
            value = NavCommand.Navigate(
                destination = when {
                    (invite as? ClassInvite)?.inviteMode == ClassInviteModeEnum.VIA_PARENT -> {
                        SignupScreen.create(
                            schoolUrl = schoolUrl,
                            inviteRequest = inviteRequest,
                            signupMode = SignupScreenModeEnum.ADD_CHILD_TO_PARENT,
                            parentPerson = personRegistered,
                        )
                    }

                    personRegistered.status == PersonStatusEnum.PENDING_APPROVAL -> {
                        WaitingForApproval()
                    }

                    else -> {
                        Home
                    }
                },
                clearBackStack = true
            )
        )
    }

}