package org.openeel.shared.domain.account.invite

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.contentType
import org.openeel.libutil.ext.appendEndpointSegments
import org.openeel.shared.domain.account.AuthResponse

/**
 * RedeemInviteUseCase should be used by the [org.openeel.shared.domain.account.AppAccountManager],
 * not directly by any ViewModel
 */
class RedeemInviteUseCaseClient(
    private val schoolUrl: Url,
    private val httpClient: HttpClient,
) : RedeemInviteUseCase {

    override suspend fun invoke(redeemRequest: RedeemInviteRequest): AuthResponse {
        return httpClient.post(
            schoolUrl.appendEndpointSegments("api/school/respect/invite/redeem")
        ) {
            contentType(ContentType.Application.Json)
            setBody(redeemRequest)
        }.body()
    }

}