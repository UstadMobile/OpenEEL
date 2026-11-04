package org.openeel.shared.domain.account

import kotlinx.serialization.Serializable
import org.openeel.datalayer.AuthenticatedUserPrincipalId
import org.openeel.datalayer.respect.model.SchoolDirectoryEntry
import org.openeel.shared.util.di.UserAccountScopeId

/**
 * Represents a single Respect account
 *
 * The RESPECT Account Manager can provide a Koin Scope for a given account.
 * @property userGuid the guid for this user as per Person.guid .
 */
@Serializable
data class UserAccount(
    val userGuid: String,
    val school: SchoolDirectoryEntry,
) {

    /**
     * The ScopeId to use for dependency injection - always the userSourcedId@realmUrl
     */
    val scopeId: String
        get() = UserAccountScopeId(
            school.self,
            AuthenticatedUserPrincipalId(userGuid),
        ).scopeId

}
