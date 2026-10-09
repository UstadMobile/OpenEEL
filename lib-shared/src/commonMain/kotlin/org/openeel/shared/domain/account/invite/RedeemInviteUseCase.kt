package world.respect.shared.domain.account.invite

import world.respect.shared.domain.account.AuthResponse

/**
 * UseCase to redeem an invitation using an invite code. This has two implementations:
 * Database (server): receives a request, creates a person with a pending status, sets the account
 * credentials.
 *
 * Http (client): sends a request to the server (which then use uses the database implementation).
 */
interface RedeemInviteUseCase {

    /**
     *
     */
    suspend operator fun invoke(
        redeemRequest: RespectRedeemInviteRequest,
        useActiveUserAuth: Boolean = false
    ): AuthResponse

}