package org.openeel.shared.domain.account.authwithopenid

import io.ktor.http.Url

interface OpenIdAuthorizationUseCase {

    suspend operator fun invoke(issuer: Url, schoolUrl: Url)
}
