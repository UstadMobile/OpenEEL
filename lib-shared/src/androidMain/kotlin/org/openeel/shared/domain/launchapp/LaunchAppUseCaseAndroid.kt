package world.respect.shared.domain.launchapp

import android.content.Context
import android.content.Intent
import android.content.Intent.CATEGORY_BROWSABLE
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.net.toUri
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.novarysearch.removeLaunchSearchParams
import com.ustadmobile.libcache.util.LaunchNoVarySearchConstants
import io.github.aakira.napier.Napier
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.serialization.json.Json
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import world.respect.datalayer.school.opds.OpdsPublicationDataSource
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.opds.model.Publication
import world.respect.lib.opds.model.findLaunchableAppLink
import world.respect.lib.xapi.model.XapiActor
import world.respect.libutil.ext.resolve
import world.respect.shared.domain.launchapp.LaunchAppUseCase.LaunchAppRequest
import world.respect.shared.domain.launchapp.getlaunchoptionsforpublication.GetLaunchOptionsForPublicationUseCase
import world.respect.shared.domain.launchapp.getxapilaunchparams.GetXapiLaunchParamsUseCase
import world.respect.shared.domain.launchapp.getxapilaunchparams.XapiLaunchParams
import world.respect.shared.domain.xapi.getxapilaunchurl.GetXapiLaunchUrlUseCase
import world.respect.xapi.ipc.shared.messages.XapiIpcIntent

/**
 * Implementation of LaunchAppUseCase for Android.
 */
class LaunchAppUseCaseAndroid(
    private val appContext: Context,
    private val ustadCache: UstadCache,
    private val getLaunchOptionsForPublicationUseCase: GetLaunchOptionsForPublicationUseCase,
    private val getXapiLaunchParamsUseCase: GetXapiLaunchParamsUseCase,
    private val json: Json,
    private val opdsPublicationDataSource: OpdsPublicationDataSource,
    private val getAndroidPackageIdForLaunchableAppUseCase: GetAndroidPackageIdForLaunchableAppUseCase,
): LaunchAppUseCase {

    private fun URLBuilder.setXapiLaunchParams(
        params: XapiLaunchParams
    ): URLBuilder {
        encodedParameters["endpoint"] = UrlEncoderUtil.encode(params.endpoint.toString())
        encodedParameters["activity_id"] = UrlEncoderUtil.encode(params.activityId)
        encodedParameters["auth"] = UrlEncoderUtil.encode(params.auth)
        encodedParameters["actor"] = UrlEncoderUtil.encode(
            json.encodeToString(XapiActor.serializer(), params.actor)
        )
        return this
    }



    private data class LaunchableAppNotInstalled(
        val launchableApp: Publication,
        val launchUrl: Url,
    )

    /**
     * Launch a compatible app for the given LaunchRequest. Tries to launch a publication as follows:
     *
     * 1) When the Url to launch uses the intent scheme, check if the intent resolves. If the intent
     * resolves, proceed to launch the intent.
     *
     * 2) If the Url uses a normal https Url try and see if the intent can be resolved using the url
     * as data with the action OpenEelIntent.ACTION_LAUNCH, if so, launch that intent (this supports
     * apps which allow a server to specify their own url to support dynamic Urls that are not
     * included in the AndroidManifest for verification).
     *
     * 3) Try launching an intent with only the deep link and the default action view.
     *
     *   On Android SDK30 and above:
     *    Attempts to launch the url using an intent with FLAG_ACTIVITY_REQUIRE_NON_BROWSER as per
     *    https://developer.android.com/training/package-visibility/use-cases#let-non-browser-apps-handle-urls
     *
     *   On Pre-SDK30 devices:
     *    Use the package manager to look for installed apps that can handle the intent (Pre-SDK30
     *    restrictions on querying packages are not enforced, so this technique works).
     *
     * 4) Open the URL using the webview activity for web-based content
     */
    override suspend fun invoke(
        request: LaunchAppRequest
    ): LaunchAppUseCase.LaunchAppResult {
        //
        val launchableAppsNotInstalled = mutableListOf<LaunchableAppNotInstalled>()

        try {
            val optionsResult = getLaunchOptionsForPublicationUseCase(
                request.publication, request.publicationUrl
            )

            val launchableAppManifest = request.launchableApp ?:
                request.publication.findLaunchableAppLink()?.let { link ->
                    opdsPublicationDataSource.getByUrl(
                        url = request.publicationUrl.resolve(link.href),
                        params = DataLoadParams()
                    ).dataOrNull()
                }

            val launchPackageId = launchableAppManifest?.let {
                getAndroidPackageIdForLaunchableAppUseCase(it)
            }

            for(launchOption in optionsResult.options) {
                val nativeLaunchParams = getXapiLaunchParamsUseCase(
                    activityId = launchOption.activityId,
                    assignmentActivityId = request.assignmentActivityId,
                    type = GetXapiLaunchUrlUseCase.LaunchType.NATIVE,
                )

                val urlWithNativeParams = URLBuilder(launchOption.url).apply {
                    setXapiLaunchParams(nativeLaunchParams)
                    parameters[XapiIpcIntent.PARAM_NAME_IPC_SERVICE_PACKAGE] = appContext.packageName
                }.build()

                ustadCache.setExtraResponseHeaders(
                    url = launchOption.url.removeLaunchSearchParams(),
                    extraResponseHeaders = headersOf(
                        "No-Vary-Search" to listOf(
                            LaunchNoVarySearchConstants.LAUNCH_LINK_NO_VARY_HEADER
                        )
                    )
                )

                when {
                    /*
                     * If the URL specified uses an intent protocol URL, check if the intent can be
                     * resolved (e.g. the app is installed). If yes, then launch it.
                    */
                    launchOption.url.protocol.name == "intent" -> {
                        val intent = Intent.parseUri(
                            urlWithNativeParams.toString(), Intent.URI_INTENT_SCHEME
                        ).also {
                            it.action = OpenEelIntent.ACTION_LAUNCH
                            it.flags = it.flags.or(FLAG_ACTIVITY_NEW_TASK)
                        }

                        val resolvedInfo = appContext.packageManager.queryIntentActivities(
                            intent, PackageManager.MATCH_DEFAULT_ONLY
                        )

                        if(resolvedInfo.isNotEmpty()) {
                            appContext.startActivity(intent)
                            Log.i(LaunchAppTags.LOGTAG, "LaunchAppUseCase: launched native app using intent uri: ${intent.toUri(Intent.URI_INTENT_SCHEME)}")
                            return LaunchAppUseCase.LaunchAppSuccess
                        }else {
                            optionsResult.launchableApp?.also {
                                launchableAppsNotInstalled.add(
                                    LaunchableAppNotInstalled(it, urlWithNativeParams)
                                )
                            }
                        }
                    }

                    /**
                     * Not using the intent protocol
                     */
                    else -> {
                        /*
                         * If the app supports using the OpenEelIntent.ACTION_LAUNCH action, use that.
                         *
                         * This allows apps to support URLs that are not directly declared in their
                         * manifest for verified app links (e.g. needed when an app supports
                         * connecting to a server specified by the user).
                         *
                         * The package id MUST be extracted from the app store link
                         */
                        val launchActionIntent = Intent(OpenEelIntent.ACTION_LAUNCH).also {
                            it.flags = FLAG_ACTIVITY_NEW_TASK
                            it.data = urlWithNativeParams.toString().toUri()
                            it.addCategory(CATEGORY_BROWSABLE)
                            it.`package` = launchPackageId
                        }

                        if(
                            launchPackageId != null && appContext.packageManager.queryIntentActivities(
                                launchActionIntent, PackageManager.MATCH_DEFAULT_ONLY
                            ).isNotEmpty()
                        ) {
                            appContext.startActivity(launchActionIntent)
                            Log.i(LaunchAppTags.LOGTAG, "LaunchAppUseCase: launched native app using intent: ${launchActionIntent.toUri(Intent.URI_INTENT_SCHEME)}")
                            return LaunchAppUseCase.LaunchAppSuccess
                        }

                        //Try launching as a normal deep link for a native app
                        val deepLinkIntent = Intent(
                            Intent.ACTION_VIEW, urlWithNativeParams.toString().toUri()
                        ).apply {
                            addCategory(CATEGORY_BROWSABLE)
                        }

                        try {
                            if(Build.VERSION.SDK_INT >= 30) {
                                deepLinkIntent.flags = FLAG_ACTIVITY_REQUIRE_NON_BROWSER or FLAG_ACTIVITY_NEW_TASK
                                appContext.startActivity(deepLinkIntent)
                                Log.i(
                                    LaunchAppTags.LOGTAG, "LaunchAppUseCaseAndroid: launched $urlWithNativeParams with RequireNonBrowser"
                                )
                                return LaunchAppUseCase.LaunchAppSuccess
                            }else {
                                deepLinkIntent.flags = FLAG_ACTIVITY_NEW_TASK
                                val resolvedInfo = appContext.packageManager.queryIntentActivities(
                                    deepLinkIntent, PackageManager.MATCH_ALL
                                )

                                val availableNativePackages = resolvedInfo.mapNotNull {
                                    it.activityInfo.packageName
                                }.filterNot { it in KNOWN_BROWSER_PACKAGES }

                                if(availableNativePackages.isNotEmpty()) {
                                    appContext.startActivity(deepLinkIntent)
                                    Log.i(
                                        LaunchAppTags.LOGTAG, "LaunchAppUseCaseAndroid: launched $urlWithNativeParams using resolved native app"
                                    )
                                    return LaunchAppUseCase.LaunchAppSuccess
                                }
                            }
                        }catch (_: Throwable) {
                            Log.d(LaunchAppTags.LOGTAG, "Unable to launch native app for $urlWithNativeParams will fallback to WebView")
                        }

                        val webViewLaunchUrl = URLBuilder(launchOption.url).apply {
                            setXapiLaunchParams(
                                getXapiLaunchParamsUseCase(
                                    activityId = launchOption.activityId,
                                    assignmentActivityId = request.assignmentActivityId,
                                    type = GetXapiLaunchUrlUseCase.LaunchType.WEBVIEW,
                                )
                            )
                        }.build()

                        /*
                         * The ActivityClass, because it's UI, is contained within the respect-app-compose module,
                         * and is referenced using reflection. Activity names are not obfuscated by R8, so this is
                         * safe.
                         */
                        val intent = Intent(
                            appContext,
                            Class.forName(WEBVIEW_ACTIVITY_NAME)
                        )
                        intent.flags = FLAG_ACTIVITY_NEW_TASK
                        val launchUrlStr = webViewLaunchUrl.toString()
                        intent.putExtra(EXTRA_URL, launchUrlStr)
                        appContext.startActivity(intent)
                        Napier.i("LaunchAppUseCaseAndroid: launching WebViewActivity for url $launchUrlStr")
                        return LaunchAppUseCase.LaunchAppSuccess
                    }
                }
            }
        }catch(e: Throwable) {
            Log.e(LaunchAppTags.LOGTAG, "Exception launching ${request.publicationUrl}", e)
            return LaunchAppUseCase.LaunchAppFailed(e)
        }

        return launchableAppsNotInstalled.firstOrNull()?.let {
            LaunchAppUseCase.LaunchAppInstallRequired(
                launchableApp = it.launchableApp,
                referrerUrl = it.launchUrl,
            )
        } ?: LaunchAppUseCase.LaunchAppFailed(null)

    }

    companion object {

        private const val WEBVIEW_ACTIVITY_NAME = "world.respect.WebViewActivity"

        const val EXTRA_URL = "url"

        private val KNOWN_BROWSER_PACKAGES = listOf(
            "org.chromium.webview_shell",//WebView on emulator
            "com.android.chrome",
            "org.mozilla.firefox",
            "com.sec.android.app.sbrowser", //Samsung browser
            "com.mi.globalbrowser",//Mi browser
        )

    }
}