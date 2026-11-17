package org.openeel.shared.domain.navigation.deeplink

import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.encodeURLParameter

/**
 * Converts a Url into an Android intent Uri restricted to an explicit package (see
 * https://developer.chrome.com/docs/android/intents ):
 *
 * https://school.example.org/some/path?param=value
 * becomes
 * intent://school.example.org/some/path?param=value#Intent;scheme=https;package={packageName};end
 *
 * When a browser navigates to it, only the given package can open the link. If it can't (e.g. app
 * not installed), Chrome opens the package's app store page. Any url fragment is replaced.
 *
 * Used by RegisterSchoolUseCase to return the user to the app that started self-registration.
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
            port = url.specifiedPort
            encodedFragment = listOf(
                "Intent",
                "scheme=${url.protocol.name.encodeURLParameter()}",
                "package=${packageName.encodeURLParameter()}",
                "end",
            ).joinToString(separator = ";")
        }.build()
    }

}
