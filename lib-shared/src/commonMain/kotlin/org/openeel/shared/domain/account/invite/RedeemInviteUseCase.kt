package org.openeel.shared.domain.account.invite

import org.openeel.shared.domain.account.AuthResponse

/**
 * UseCase to redeem an invitation using an invite code. This has two implementations:
 * Database (server): receives a request, creates a person with a pending status, sets the account
 * credentials.
 *
 * Http (client): sends a request to the server (which then use uses the database implementation).
 */
interface RedeemInviteUseCase {

    /**
     * @param redeemRequest invite request to redeem
     * @param useActiveUserAuth true if currently active user's authentication should be used when
     *        making the redeem request. This is required when enabling shared device mode: the
     *        teacher/admin's credentials are required for the invite redeem to go through without
     *        requiring approval.
     */
    suspend operator fun invoke(
        redeemRequest: RedeemInviteRequest,
        useActiveUserAuth: Boolean = false
    ): AuthResponse

}