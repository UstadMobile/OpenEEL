package world.respect.server.domain.school.demoapp

import com.eygraber.uri.Uri
import io.ktor.http.Url
import org.openeel.demo.demolaunchableappserver.DemoConstants
import world.respect.lib.opds.model.LangMapStringValue
import world.respect.lib.opds.model.Publication
import world.respect.lib.opds.model.ReadiumContributorObject
import world.respect.lib.opds.model.ReadiumLink
import world.respect.lib.opds.model.ReadiumMetadata
import world.respect.libutil.ext.resolve

class MakeDemoAppManifestUseCase(
    private val demoStrings: DemoStringMaps,
) {

    operator fun invoke(
        baseUrl: Url,
        langCode: String = "en-US",
    ) : Publication {
        val strings = demoStrings.requireLangMap(langCode)

        return Publication(
            metadata = ReadiumMetadata(
                title = LangMapStringValue(
                    strings.requireString("app_name")
                ),
                author = listOf(
                    ReadiumContributorObject(
                        name = "UstadMobile FZ-LLC",
                        links = listOf(
                            ReadiumLink(
                                href = "https://www.ustadmobile.com/"
                            )
                        )
                    )
                ),
                identifier = Uri.parse("https://demo.openeel.org/app"),
                language = listOf(langCode),
                modified = "2025-09-29T17:00:00Z"
            ),
            links = DemoConstants.LANGUAGE_CODES.filterNot { it == langCode }.map { otherLang ->
                ReadiumLink(
                    rel = listOf("alternate"),
                    href = baseUrl.resolve("$otherLang/$APP_MANIFEST_FILENAME").toString(),
                    language = listOf(otherLang),
                )
            } + listOf(
                ReadiumLink(
                    href = baseUrl.resolve("$langCode/$APP_MANIFEST_FILENAME").toString(),
                    rel = listOf("self"),
                    type = "application/opds-publication+json"
                ),
                ReadiumLink(
                    rel = listOf("collection"),
                    type = "application/opds+json",
                    href = baseUrl.resolve("$langCode/default-collection.json").toString()
                ),
                ReadiumLink(
                    rel = listOf("https://id.openeel.org/rel/app-launch-uri"),
                    href = baseUrl.toString(),
                ),
                ReadiumLink(
                    rel = listOf("https://id.openeel.org/rel/appstore-android"),
                    href = "https://play.google.com/store/apps/details?id=org.openeel.demolaunchableapp",
                    title = "Get it on Google Play",
                ),
                ReadiumLink(
                    rel = listOf("terms-of-service"),
                    href = baseUrl.resolve("terms-privacy.html").toString(),
                ),
                ReadiumLink(
                    rel = listOf("license"),
                    href = "https://opensource.org/license/mit"
                )
            ),
            images = listOf(
                ReadiumLink(
                    href = baseUrl.resolve(APP_MANIFEST_ICON_NAME).toString(),
                    type = "image/png",
                )
            )
        )
    }

    companion object {

        const val APP_MANIFEST_FILENAME = "launchable-app-manifest.json"

        const val APP_MANIFEST_ICON_NAME = "app_icon.png"
    }
}