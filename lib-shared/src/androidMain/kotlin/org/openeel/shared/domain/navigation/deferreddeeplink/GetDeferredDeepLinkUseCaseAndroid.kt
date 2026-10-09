package world.respect.shared.domain.navigation.deferreddeeplink

import android.content.Context
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.russhwolf.settings.Settings
import io.github.aakira.napier.Napier
import io.ktor.http.parseQueryString
import kotlinx.coroutines.CompletableDeferred
import world.respect.shared.domain.navigation.deferreddeeplink.GetDeferredDeepLinkUseCase.Companion.PARAM_NAME_DEFERRED_DEEP_LINK

class GetDeferredDeepLinkUseCaseAndroid(
    context: Context,
    private val settings: Settings
) : GetDeferredDeepLinkUseCase {

    private val completeable = CompletableDeferred<String?>()

    init {
        val fetchDone = settings.getBoolean(KEY_CHECK_DONE, false)
        if(fetchDone) {
            completeable.complete(
                value = settings.getStringOrNull(KEY_DEFERRED_DEEP_LINK).also {
                    Napier.i("DeferredDeepLink: fetch was already done: deep link is: $it")
                }
            )
        }else {
            val client = InstallReferrerClient
                .newBuilder(context)
                .build()

            //As per
            // https://developer.android.com/google/play/installreferrer/library#connecting
            client.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(code: Int) {
                    when(code) {
                        InstallReferrerClient.InstallReferrerResponse.OK -> {
                            val response: ReferrerDetails = client.installReferrer
                            val referrerUrl: String? = response.installReferrer
                            Napier.i("DeferredDeepLink: referrerUrl = $referrerUrl")
                            val deferredDeepLink = referrerUrl?.let {
                                try {
                                    parseQueryString(referrerUrl)[PARAM_NAME_DEFERRED_DEEP_LINK].also {
                                        Napier.i("DeferredDeepLink: deferredDeepLink = $it")
                                    }
                                }catch(e: Throwable) {
                                    Napier.e("DeferredDeepLink: Exception parsing referral", e)
                                    null
                                }
                            }

                            if(deferredDeepLink != null) {
                                settings.putString(KEY_DEFERRED_DEEP_LINK, deferredDeepLink)
                            }

                            completeable.complete(deferredDeepLink)

                            settings.putBoolean(KEY_CHECK_DONE, true)
                        }

                        InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                            Napier.i("DeferredDeepLink: feature not supported")
                            settings.putBoolean(KEY_CHECK_DONE, true)
                        }

                        InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                            Napier.i("DeferredDeepLink: service unavailable")
                        }
                    }
                }

                override fun onInstallReferrerServiceDisconnected() {
                    Napier.i("DeferredDeepLink: service disconnected")
                }
            })
        }
    }

    override suspend fun invoke(): String? {
        return completeable.await()
    }

    companion object {

        const val KEY_CHECK_DONE = "install_referrer_fetched"

        const val KEY_DEFERRED_DEEP_LINK = "install_deferred_deep_link"
    }
}