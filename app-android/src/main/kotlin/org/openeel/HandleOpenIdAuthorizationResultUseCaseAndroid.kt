package org.openeel

import android.content.Context
import android.content.Intent
import io.ktor.http.Url
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import org.openeel.OpenIdAuthorizationUseCaseAndroid.Companion.openIdAppAuthConfiguration
import org.openeel.shared.domain.account.AuthResponse
import org.openeel.shared.domain.account.authwithopenid.GetTokenAndUserProfileWithOpenIdUseCase
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class HandleOpenIdAuthorizationResultUseCaseAndroid(
    context: Context,
    private val getTokenAndUserProfileWithOpenIdUseCase: GetTokenAndUserProfileWithOpenIdUseCase,
) {

    private val appContext = context.applicationContext

    sealed interface Result {
        data object NotAuthorizationCallback : Result
        data object Canceled : Result
        data class Authenticated(
            val schoolUrl: Url,
            val authResponse: AuthResponse,
        ) : Result
    }

    suspend operator fun invoke(intent: Intent): Result {
        if (intent.action == OpenIdAuthorizationUseCaseAndroid.ACTION_OPENID_AUTHORIZATION_CANCELED) {
            return Result.Canceled
        }
        if (intent.action != OpenIdAuthorizationUseCaseAndroid.ACTION_OPENID_AUTHORIZATION_RESULT) {
            return Result.NotAuthorizationCallback
        }

        val issuerText = intent.getStringExtra(OpenIdAuthorizationUseCaseAndroid.EXTRA_ISSUER)
        val schoolText = intent.getStringExtra(OpenIdAuthorizationUseCaseAndroid.EXTRA_SCHOOL_URL)
        if (issuerText == null || schoolText == null) {
            throw IllegalArgumentException("OpenID callback is missing its issuer or school URL")
        }

        val issuer = Url(issuerText)
        val schoolUrl = Url(schoolText)
        val authorizationResponse = AuthorizationResponse.fromIntent(intent)
        if (authorizationResponse == null) {
            val authorizationException = AuthorizationException.fromIntent(intent)
            throw IllegalStateException(
                authorizationException?.message ?: "OpenID authorization returned no response"
            )
        }

        val authorizationService = AuthorizationService(
            appContext,
            openIdAppAuthConfiguration(issuer = issuer),
        )
        val accessToken = try {
            suspendCancellableCoroutine<String> { continuation ->
                // AppAuth returns the token exchange result through this callback.
                // https://github.com/openid/AppAuth-Android#exchanging-the-authorization-code
                authorizationService.performTokenRequest(
                    authorizationResponse.createTokenExchangeRequest()
                ) { response, exception ->
                    if (continuation.isActive) {
                        val token = response?.accessToken
                        if (token != null) {
                            continuation.resume(value = token)
                        } else {
                            continuation.resumeWithException(
                                exception = IllegalStateException(
                                    exception?.message
                                        ?: "OpenID token exchange returned no access token"
                                )
                            )
                        }
                    }
                }
            }
        } finally {
            authorizationService.dispose()
        }

        val authResponse = getTokenAndUserProfileWithOpenIdUseCase(
            schoolUrl = schoolUrl,
            accessToken = accessToken,
        )

        return Result.Authenticated(
            schoolUrl = schoolUrl,
            authResponse = authResponse,
        )
    }
}
