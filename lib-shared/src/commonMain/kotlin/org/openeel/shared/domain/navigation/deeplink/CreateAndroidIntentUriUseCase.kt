package org.openeel.shared.domain.navigation.deeplink

import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.encodeURLParameter

/**
 * Convert a Url into an Android intent Uri (as per Intent.toUri(Intent.URI_INTENT_SCHEME) - see
 * https://developer.android.com/reference/android/content/Intent#toUri(int) and
 * https://developer.chrome.com/docs/android/intents ) that is restricted to an explicit package.
 *
 * https://school.example.org/some/path?param=value
 * becomes
 * intent://school.example.org/some/path?param=value#Intent;scheme=https;package={packageName};end
 *
 * When a browser (including a Chrome Custom Tab) navigates to the intent Uri, the link is opened
 * (using the default action android.intent.action.VIEW) only by the given package (which must have
 * a matching intent filter). No custom protocol or verified app link is required. If the intent
 * cannot be resolved (e.g. the app is not installed, or does not declare the url's host in its
 * manifest), Chrome opens the app store page for the package instead.
 *
 * The intent fields are stored in the Uri fragment, so any fragment on the given url is replaced
 * (school links are not expected to have a fragment).
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
     */
    operator fun invoke(
        url: Url,
        packageName: String,
    ): Url {
        return URLBuilder(url).apply {
            protocol = URLProtocol.createOrDefault("intent")
            // Otherwise the default port of the original protocol (e.g. 443) would be added
            port = url.specifiedPort
            // Values are already encoded: the fragment setter would encode them again
            encodedFragment = listOf(
                "Intent",
                "scheme=${url.protocol.name.encodeURLParameter()}",
                "package=${packageName.encodeURLParameter()}",
                "end",
            ).joinToString(separator = ";")
        }.build()
    }

}
