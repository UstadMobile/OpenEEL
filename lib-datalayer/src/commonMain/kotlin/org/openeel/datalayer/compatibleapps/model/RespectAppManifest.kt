package world.respect.datalayer.compatibleapps.model

import com.eygraber.uri.Uri
import io.ktor.http.Url
import kotlinx.serialization.Serializable
import world.respect.lib.opds.model.LangMap
import world.respect.lib.serializers.UriStringSerializer

/**
 * Represents the RESPECT manifest as described in the RESPECT Launcher App Integration Guide.
 * This data class defines the structure of the manifest file, which provides metadata about a
 * RESPECT-compliant application.
 *
 * For more details, refer to the official documentation:
 * [RESPECT Consumer App Integration Guide](https://github.com/UstadMobile/RESPECT-Consumer-App-Integration-Guide/)
 *
 * @property name A [LangMap] containing the name of the application in one or more languages
 * @property license The license under which the application is distributed using its identifier from
 *           https://spdx.org/licenses or "proprietary" if not using a listed license.
 * @property description A [LangMap] providing a description of the application in one or more languages.
 * @property website The official website URL for the application.
 * @property icon The URL to the application's icon. This may be omitted IF the website contains a
 *           a favicon with a 512x512 or higher resolution.
 * @property learningUnits A URI pointing to the OPDS feed of learning units provided by the application.
 * @property defaultLaunchUri A URI that the RESPECT launcher will use to start the application.
 * @property android Optional [AndroidDetails] containing Android-specific information if the application is an Android app.
 * @property web Optional [WebDetails] containing web-specific information if the application is a web app.
 */
@Serializable
data class RespectAppManifest(
    val name: LangMap,
    val description: LangMap? = null,
    val license: String,
    val website: Url? = null,
    @Serializable(with = UriStringSerializer::class)
    val icon: Uri? = null,
    @Serializable(with = UriStringSerializer::class)
    val learningUnits: Uri,
    @Serializable(with = UriStringSerializer::class)
    val defaultLaunchUri: Uri,
    val android: AndroidDetails? = null,
    val web: WebDetails? = null,
    val screenshots: List<Screenshot>? = null,
) {
    /**
     * Contains Android-specific details for the RESPECT manifest.
     * @param packageId Android package id
     * @param stores a list of URLs for stores from where the app can be obtained (e.g. Google Play,
     *        F-Droid, etc)
     * @param sourceCode URL for the source code, if available.
     */
    @Serializable
    data class AndroidDetails(
        val packageId: String,
        val stores: List<Url> = emptyList(),
        val sourceCode: Url? = null,
    )

    /**
     * Contains web-specific details for the RESPECT manifest.
     *
     * @param sourceCode URL for the source code, if available.
     */
    @Serializable
    data class WebDetails(
        val url: Url? = null,
        val sourceCode: Url? = null,
    )

    @Serializable
    data class Screenshot(
        val url: Url,
        val description: LangMap,
    )

    companion object {

        const val MIME_TYPE = "application/json"

    }

}

