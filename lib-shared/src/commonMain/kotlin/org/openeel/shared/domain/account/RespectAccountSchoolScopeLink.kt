package world.respect.shared.domain.account

import io.ktor.http.Url

/**
 * Wrapper class that is used for dependency injection purposes. See AppKoinModule for details.
 */
data class RespectAccountSchoolScopeLink(
    val url: Url
)
