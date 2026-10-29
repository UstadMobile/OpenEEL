package org.openeel

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.net.toUri
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import org.openeel.datalayer.respect.model.OpenIdAuthOptionConfig
import org.openeel.shared.domain.account.authwithopenid.LaunchOpenIdAuthorizationUseCase
import org.openeel.shared.domain.activitycontextjobprocessor.EnqueueActivityContextJobUseCase
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LaunchOpenIdAuthorizationUseCaseAndroid(
    private val schoolUrl: Url,
    private val enqueueActivityContextJobUseCase: EnqueueActivityContextJobUseCase,
) : LaunchOpenIdAuthorizationUseCase {

    override suspend fun invoke(config: OpenIdAuthOptionConfig) {
        val issuerUri = config.issuer.toString().toUri()
        val serviceConfiguration = suspendCancellableCoroutine { continuation ->
            // This callback provides the configuration used to build the authorization request.
            // https://github.com/openid/AppAuth-Android#authorization-service-configuration
            val configurationCallback =
                AuthorizationServiceConfiguration.RetrieveConfigurationCallback {
                    configuration, exception ->
                    if (configuration != null) {
                        continuation.resume(value = configuration)
                    } else {
                        continuation.resumeWithException(
                            exception = IllegalStateException(
                                exception?.message ?: "Unable to load OpenID configuration"
                            )
                        )
                    }
                }


            // This is a Java API, so Kotlin named arguments are unavailable. The local names
            // above show what each positional argument represents.
            AuthorizationServiceConfiguration.fetchFromIssuer(
                issuerUri,
                configurationCallback,
            ) { uri ->
                URL(uri.toString()).openConnection() as HttpURLConnection
            }
        }

        val authorizationRequest = AuthorizationRequest.Builder(
            serviceConfiguration,
            config.clientId,
            ResponseTypeValues.CODE,
            config.redirectUri.toUri(),
        ).apply {
            setScope(config.scope)
            setPrompt(config.prompt)
        }.build()

        enqueueActivityContextJobUseCase(request = { activity: Activity ->
            val authorizationService = AuthorizationService(
                activity,
                openIdAppAuthConfiguration(issuer = config.issuer),
            )

            //You can use Class.forName to lookup MainActivity if needed same as is done in LaunchAppUseCaseAndroid
            val completedIntent = Intent(activity, MainActivity::class.java).apply {
                action = ACTION_OPENID_AUTHORIZATION_RESULT
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra(EXTRA_ISSUER, config.issuer.toString())
                putExtra(EXTRA_SCHOOL_URL, schoolUrl.toString())
            }
            val canceledIntent = Intent(activity, MainActivity::class.java).apply {
                action = ACTION_OPENID_AUTHORIZATION_CANCELED
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra(EXTRA_ISSUER, config.issuer.toString())
                putExtra(EXTRA_SCHOOL_URL, schoolUrl.toString())
            }

            // AppAuth adds the authorization response to its completion PendingIntent, so it
            // must be mutable. Android 12+ requires an explicit mutability flag, older versions
            // use 0 because they do not require one. UPDATE_CURRENT refreshes the school/provider
            // extras if Android reuses a PendingIntent.
            // https://developer.android.com/reference/android/app/PendingIntent#FLAG_UPDATE_CURRENT
            // https://github.com/openid/AppAuth-Android#obtaining-an-authorization-code
            val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_MUTABLE
                } else {
                    0
                })

            val completedPendingIntent = PendingIntent.getActivity(
                activity,
                AUTHORIZATION_RESULT_REQUEST_CODE,
                completedIntent,
                pendingIntentFlags,
            )
            val canceledPendingIntent = PendingIntent.getActivity(
                activity,
                AUTHORIZATION_CANCELED_REQUEST_CODE,
                canceledIntent,
                pendingIntentFlags,
            )

            try {
                authorizationService.performAuthorizationRequest(
                    authorizationRequest,
                    completedPendingIntent,
                    canceledPendingIntent,
                )
            } catch (exception: Exception) {
                Napier.e(
                    message = "Unable to start OpenID sign-in",
                    throwable = exception,
                )
            } finally {
                authorizationService.dispose()
            }
        })
    }

    companion object {
        const val ACTION_OPENID_AUTHORIZATION_RESULT = "org.openeel.OPENID_AUTHORIZATION_RESULT"
        const val ACTION_OPENID_AUTHORIZATION_CANCELED = "org.openeel.OPENID_AUTHORIZATION_CANCELED"
        const val EXTRA_ISSUER = "openid_issuer"
        const val EXTRA_SCHOOL_URL = "openid_school_url"
        const val AUTHORIZATION_RESULT_REQUEST_CODE = 0
        const val AUTHORIZATION_CANCELED_REQUEST_CODE = 1

        internal fun openIdAppAuthConfiguration(issuer: Url): AppAuthConfiguration =
            AppAuthConfiguration.Builder()
                .setConnectionBuilder { uri ->
                    URL(uri.toString()).openConnection() as HttpURLConnection
                }
                .setSkipIssuerHttpsCheck(issuer.toString().startsWith("http://"))
                .build()
    }
}
