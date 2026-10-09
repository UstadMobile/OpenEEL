package world.respect.shared.domain.school

import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import io.github.aakira.napier.Napier
import io.ktor.http.Url

class LaunchCustomTabUseCaseAndroid(
    private val appContext: Context
) : LaunchCustomTabUseCase {

    override fun invoke(url: Url) {
        try {
            // The package must be explicitly set otherwise, if the URL is included in the verified
            // app links then Chrome will not open and Android will try to send the intent to the
            // RESPECT app itself. The Custom Chrome Tab is used to as per single sign on and auth
            // cases so that any active logins etc from the browser will be available.
            val customTabPackage = CustomTabsClient.getPackageName(
                appContext, null
            ) ?: throw IllegalStateException("No custom chrome tab package found.")

            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setShareState(CustomTabsIntent.SHARE_STATE_ON)
                .setUrlBarHidingEnabled(true)
                .build()

            customTabsIntent.intent.setPackage(customTabPackage)
            customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            customTabsIntent.launchUrl(appContext, url.toString().toUri())
            Napier.i("LaunchCustomTabUseCaseAndroid: Launched custom tab using package $customTabPackage for $url")
        } catch (e: Exception) {
            Napier.e(throwable = e) { "LaunchCustomTabUseCaseAndroid: Error launching custom tab" }
            throw e
        }
    }
}