package world.respect.shared.util.di

import io.ktor.http.Url
import world.respect.datalayer.AuthenticatedUserPrincipalId

/**
 * Data class that handles Koin scope id for a SchoolDirectoryEntry scope.
 *
 * @property schoolUrl schoolUrl as per SchoolDirectoryEntry.self
 * @property accountPrincipalId where the scope needs to be isolated on a per-account basis, this is
 *           non-null (e.g. as expected on the client where the server does not make permission
 *           related information available, resulting in a situation where each account, even if on
 *           the same school url, must be kept in a separate database).
 */
data class SchoolDirectoryEntryScopeId(
    val schoolUrl: Url,
    val accountPrincipalId: AuthenticatedUserPrincipalId?
) {

    /**
     * Scope id to use for Koin/DI. Always prefixed with s: because Koin scopes are handled by
     * string ids and qualifiers will not isolate the two.
     */
    val scopeId = buildString {
        append("s:")
        accountPrincipalId?.also {
            append("${it.guid}@")
        }
        append(schoolUrl)
    }

    companion object {

        fun parse(scopeId: String): SchoolDirectoryEntryScopeId {
            return scopeId.substringAfter(":").let {
                val atIndex = it.lastIndexOf('@')
                if(atIndex == -1)
                    SchoolDirectoryEntryScopeId(schoolUrl = Url(it), null)
                else
                    SchoolDirectoryEntryScopeId(
                        schoolUrl = Url(it.substring(atIndex + 1)),
                        accountPrincipalId = AuthenticatedUserPrincipalId(it.substring(0, atIndex))
                    )
            }
        }

    }

}