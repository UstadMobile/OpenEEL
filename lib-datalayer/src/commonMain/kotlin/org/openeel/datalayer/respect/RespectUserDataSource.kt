package world.respect.datalayer.respect

import kotlinx.coroutines.flow.Flow
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.datalayer.respect.model.invite.RespectInviteInfo

interface RespectUserDataSource {

    /**
     * Server processing invites will return info about the invitation to display to the user
     *
     * Entities (e.g. Enrollment, User, etc) are created AFTER approval.
     *
     * 1) Client sends invite code to systemurl; which then delegates to a specific server as per
     *    code (e.g. code starts with AE goes to country server for AE, etc)
     * 2) Client sends a RespectRedeemInviteRequest to server
     * 3) When teacher or authorized user approves invite, request is enqueued to go to server
     * 4) Server generates user entities. Server response includes generated entities.
     *
     */
    suspend fun getInviteInfo(code: String): Flow<DataLoadState<RespectInviteInfo>>


}