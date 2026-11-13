package org.openeel.shared.domain.navigation.deeplink

import io.ktor.http.Url
import io.ktor.http.encodeURLParameter
import org.openeel.shared.domain.launchapp.OpenEelIntent

/**
 * Convert a Url into an Android intent Uri (as per Intent.toUri(Intent.URI_INTENT_SCHEME) - see
 * https://developer.android.com/reference/android/content/Intent#toUri(int) and
 * https://developer.chrome.com/docs/android/intents ) that is restricted to an explicit package.
 *
 * https://school.example.org/some/path?param=value
 * becomes
 * intent://school.example.org/some/path?param=value#Intent;scheme=https;action={action};package={packageName};S.browser_fallback_url=https%3A%2F%2Fschool.example.org%2Fsome%2Fpath%3Fparam%3Dvalue;end
 *
 * When a browser (including a Chrome Custom Tab) navigates to the intent Uri, the link is opened
 * only by the given package (which must have a matching intent filter). No custom protocol or
 * verified app link is required. If the intent cannot be resolved (e.g. the app is not installed),
 * the browser loads the browser fallback url instead.
 *
 * Used by RegisterSchoolUseCase to return the user to the app that started school
 * self-registration.
 */
class CreateAndroidIntentUriUseCase {

    /**
     * @param url the Url to open (used as the intent data)
     * @param packageName the Android package name the intent is restricted to. It is URL encoded,
     *        so it cannot add other fields to the intent Uri. Callers should validate it first (see
     *        RespectAppManifestValidator.PACKAGE_ID_ALLOWED_CHARS).
     * @param action the intent action. Defaults to OpenEelIntent.ACTION_OPEN_LINK. If null, the
     *        action is omitted (the default action is android.intent.action.VIEW).
     * @param browserFallbackUrl the Url the browser should load if the intent cannot be resolved.
     *        Defaults to url. If null, Chrome will open the app store page for packageName instead.
     */
    operator fun invoke(
        url: Url,
        packageName: String,
        action: String? = OpenEelIntent.ACTION_OPEN_LINK,
        browserFallbackUrl: Url? = url,
    ): Url {
        val scheme = url.protocol.name

        return Url(
            buildString {
                append("intent:")
                append(url.toString().removePrefix("$scheme:"))
                append("#Intent;scheme=${scheme.encodeURLParameter()};")
                action?.also { append("action=${it.encodeURLParameter()};") }
                append("package=${packageName.encodeURLParameter()};")
                browserFallbackUrl?.also {
                    append("S.$EXTRA_BROWSER_FALLBACK_URL=${it.toString().encodeURLParameter()};")
                }
                append("end")
            }
        )
    }

    companion object {

        /**
         * Intent extra used by Chrome (and other browsers) as the Url to load if the intent cannot
         * be resolved.
         */
        const val EXTRA_BROWSER_FALLBACK_URL = "browser_fallback_url"

    }
}
