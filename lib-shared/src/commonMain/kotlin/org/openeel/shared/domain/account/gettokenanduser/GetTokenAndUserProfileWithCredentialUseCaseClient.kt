package world.respect.shared.domain.account.gettokenanduser

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.retry
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.http.takeFrom
import world.respect.credentials.passkey.RespectCredential
import world.respect.shared.domain.account.AuthResponse
import world.respect.datalayer.school.model.DeviceInfo
import world.respect.lib.dataloadstate.throwable.ForbiddenException
import world.respect.shared.domain.getdeviceinfo.GetDeviceInfoUseCase

class GetTokenAndUserProfileWithCredentialUseCaseClient(
    private val schoolUrl: Url,
    private val httpClient: HttpClient,
    private val getDeviceInfoUseCase: GetDeviceInfoUseCase?,
): GetTokenAndUserProfileWithCredentialUseCase {

    override suspend fun invoke(
        credential: RespectCredential,
        deviceInfo: DeviceInfo?,
    ): AuthResponse {
        val response = httpClient.post {
            retry {
                retryOnExceptionOrServerErrors(maxRetries = DEFAULT_MAX_RETRIES)
            }

            url {
                takeFrom(schoolUrl)
                appendPathSegments("api/school/respect/auth/auth-with-password")
            }

            (deviceInfo ?: getDeviceInfoUseCase?.invoke())?.also { deviceInfo ->
                header(
                    key = DeviceInfo.HEADER_NAME,
                    value = deviceInfo.toHeaderLine(),
                )
            }

            contentType(ContentType.Application.Json)
            setBody(credential)
        }

        return when {
            response.status.isSuccess() -> response.body()
            response.status == HttpStatusCode.Forbidden -> throw ForbiddenException()
            else -> throw IllegalStateException(response.status.description)
        }
    }

    companion object {

        const val DEFAULT_MAX_RETRIES = 3
    }
}