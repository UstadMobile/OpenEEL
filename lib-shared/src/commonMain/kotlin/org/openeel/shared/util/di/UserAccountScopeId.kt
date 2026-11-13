package org.openeel.shared.util.di

import io.ktor.http.Url
import org.openeel.datalayer.AuthenticatedUserPrincipalId

/**
 * Data class that handles Koin scope id for Respect account scope.
 *
 * @property schoolUrl the SchoolUrl as per SchoolDirectoryEntry.self
 * @property accountPrincipalId the authenticated user principal id as per Person.guid, or null if
 *           there is no authenticated user (eg anonymous such as when a http request is received
 *           without an Authorization header).
 */
data class UserAccountScopeId(
    val schoolUrl: Url,
    val accountPrincipalId: AuthenticatedUserPrincipalId?,
) {

    val scopeId = "a:${accountPrincipalId?.guid ?: ""}@${schoolUrl}"

    companion object {

        fun parse(scopeId: String): UserAccountScopeId {
            return scopeId.substringAfter(":").let {
                val atIndex = it.lastIndexOf('@')
                UserAccountScopeId(
                    accountPrincipalId = if(atIndex > 0)
                        AuthenticatedUserPrincipalId(it.substring(0, atIndex))
                    else
                        null,
                    schoolUrl = Url(it.substring(atIndex + 1)),
                )
            }
        }

    }
}