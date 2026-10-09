package world.respect.shared.domain.account

import kotlinx.serialization.Serializable

/**
 * Sometimes the active account (and token) is not the actual user of a session: e.g.
 *
 * Logged in as a parent, then switch to child profile
 * Logged in as device account (shared school device) - student starts session by selecting name from list
 *
 * @param account the account associated with credentials that were used to get a token and actually
 *        run the session
 * @param profilePersonUid where the active user of the session is not the account holder, this
 *        will be the personuid of the active user (e.g. the child's personUid when the session
 *        was started through the parent's account).
 */
@Serializable
data class RespectSession(
    val account: RespectAccount,
    val profilePersonUid: String?,
)

