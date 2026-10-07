package org.openeel.shared.domain.account

import io.ktor.http.Url

/**
 * Wrapper class that is used for dependency injection purposes. See AppKoinModule for details.
 */
data class UserAccountSchoolScopeLink(
    val url: Url
)
