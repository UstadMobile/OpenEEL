package org.openeel.shared.domain.navigation.onaccountcreated

import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableSharedFlow
import org.openeel.datalayer.school.model.ClassInvite
import org.openeel.datalayer.school.model.ClassInviteModeEnum
import org.openeel.datalayer.school.model.Person
import org.openeel.datalayer.school.model.PersonStatusEnum
import org.openeel.shared.domain.account.invite.RespectRedeemInviteRequest
import org.openeel.shared.navigation.Home
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.SignupScreen
import org.openeel.shared.navigation.WaitingForApproval
import org.openeel.shared.viewmodel.manageuser.signup.SignupScreenModeEnum

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