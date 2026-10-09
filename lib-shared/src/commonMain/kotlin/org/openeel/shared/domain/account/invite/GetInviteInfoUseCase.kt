package world.respect.shared.domain.account.invite

import world.respect.datalayer.respect.model.invite.RespectInviteInfo

/**
 * Retrieves information about an invite.
 *
 * Client will find the correct RESPECT server to send from the list of available servers as per
 * SystemUrlConfig.baseUrls.
 *
 * Client sends invite code to SystemUrl; which then delegates to a specific server as per
 * code (e.g. code starts with AE goes to country server for AE, etc). The client receives a
 * RespectInviteInfo response.
 *
 * Once the user completes required info in the UI, the SubmitRedeemInviteRequestUseCase should
 * be used.
 */
interface GetInviteInfoUseCase {

    suspend operator fun invoke(code: String): RespectInviteInfo

}