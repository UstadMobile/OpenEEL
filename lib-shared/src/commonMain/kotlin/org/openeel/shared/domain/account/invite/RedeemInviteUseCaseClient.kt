package org.openeel.shared.domain.account.invite

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.contentType
import org.openeel.datalayer.AuthTokenProvider
import org.openeel.libutil.ext.appendEndpointSegments
import org.openeel.shared.domain.account.AuthResponse
import org.openeel.shared.domain.account.RespectAccountManager

/**
 * RedeemInviteUseCase should be used by the RespectAccountManager, not directly by any ViewModel
 */
class RedeemInviteUseCaseClient(
    private val schoolUrl: Url,
    private val httpClient: HttpClient,
    private val accountManager: RespectAccountManager,
) : RedeemInviteUseCase {

    override suspend fun invoke(
        redeemRequest: RespectRedeemInviteRequest,
        useActiveUserAuth: Boolean
    ): AuthResponse {
        return httpClient.post(
            schoolUrl.appendEndpointSegments("api/school/respect/invite/redeem")
        ) {
            contentType(ContentType.Application.Json)

            if (!useActiveUserAuth) {
                val scope = accountManager.requireActiveAccountScope()
                val tokenProvider = scope.getOrNull<AuthTokenProvider>()
                    ?: throw IllegalStateException(
                        "useActiveUserAuth=true but no token provider found for active account"
                    )

                val token = tokenProvider.provideToken()
                headers.append(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            }

            setBody(redeemRequest)
        }.body()
    }
}