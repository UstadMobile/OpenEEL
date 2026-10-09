package world.respect.shared.domain.launchapp.gotoappstore

import android.content.Context
import android.content.Intent
import android.content.Intent.CATEGORY_BROWSABLE
import android.util.Log
import androidx.core.net.toUri
import io.ktor.http.URLBuilder
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import world.respect.lib.opds.model.findAppStoreAndroidLinks

/**
 *
 * Take the user to an app store as per the links provided by an app developer.
 *
 * Note: it would have been nice to use inline installs:
 * https://developer.android.com/distribute/marketing-tools/inline-installs
 *
 * But these are on available to the big fish as per:
 * https://developer.android.com/quality/core-value/app-eligibility
 */
class GoToAppStoreUseCaseAndroid(
    private val appContext: Context,
): GoToAppStoreUseCase {

    override suspend fun invoke(request: GoToAppStoreUseCase.Request) {
        val appStoreLink = request.preferredStoreLink ?: request.launchableApp
            .findAppStoreAndroidLinks().firstOrNull()

        if(appStoreLink != null) {
            val intent = Intent(Intent.ACTION_VIEW).also {
                it.data = URLBuilder(appStoreLink.href).apply {
                    encodedParameters["referrer"] = UrlEncoderUtil.encode(request.referrer)
                }.build().toString().toUri()
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                it.addCategory(CATEGORY_BROWSABLE)
            }

            appContext.startActivity(intent)
            Log.i("GoToAppStoreUseCase", "Submitted activity job to launch ${intent.data}")
        }else {
            Log.i("GoToAppStoreUseCase", "No app store links found for ${request.launchableApp.metadata.title}")
        }
    }
}