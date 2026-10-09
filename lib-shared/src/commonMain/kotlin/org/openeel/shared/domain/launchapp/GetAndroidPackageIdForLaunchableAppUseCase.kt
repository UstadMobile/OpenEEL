package world.respect.shared.domain.launchapp

import io.github.aakira.napier.Napier
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import world.respect.lib.opds.model.Publication
import world.respect.lib.opds.model.findAppStoreAndroidLinks

/**
 * Get the package id for a launchable app manifest (as represented by a [Publication]) by
 * looking at the app store links in the manifest.
 *
 * This will work if the app store link is to Google Play or similar eg.
 * https://play.google.com/store/apps/details?id=demo.openeel.org
 *
 * or, when an app is not published on an app store, it can use a market:// url eg.
 * market://details?id=demo.openeel.org
 *
 */
class GetAndroidPackageIdForLaunchableAppUseCase {

    operator fun invoke(publication: Publication): String? {
        return publication.findAppStoreAndroidLinks().firstNotNullOfOrNull { link ->
            val url = try {
                Url(link.href)
            } catch(e: Throwable) {
                Napier.w(throwable = e) { "Failed to parse app store url" }
                null
            }

            url?.parameters?.get("id")?.takeIf {
                url.protocol == URLProtocol.HTTPS || url.protocol.name == "market"
            }
        }

    }
}