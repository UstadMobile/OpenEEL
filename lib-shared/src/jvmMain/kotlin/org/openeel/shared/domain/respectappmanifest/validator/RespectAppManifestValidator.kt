package org.openeel.shared.domain.respectappmanifest.validator

import io.ktor.client.HttpClient
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import org.openeel.domain.getfavicons.GetFavIconUseCase
import org.openeel.domain.licenses.model.SpdxLicenseList
import org.openeel.lib.opds.model.toStringMap
import org.openeel.domain.opds.validator.verifyMimeTypeAndGetBodyAsText
import org.openeel.domain.validator.Validator
import org.openeel.datalayer.compatibleapps.model.RespectAppManifest
import org.openeel.lib.opds.model.OpdsFeed
import org.openeel.lib.opds.model.ReadiumLink
import org.openeel.shared.domain.validator.ValidateHttpResponseForUrlUseCase
import org.openeel.domain.validator.ValidatorMessage
import org.openeel.domain.validator.ValidatorReporter
import org.openeel.domain.validator.ValidateLinkUseCase
import java.net.URI

class RespectAppManifestValidator(
    private val json: Json,
    private val validateHttpResponseForUrlUseCase: ValidateHttpResponseForUrlUseCase,
    private val getFavIconUseCase: GetFavIconUseCase,
    private val httpClient: HttpClient,
) : Validator {


    /**
     * Validate the a RespectAppManifest as per the KDoc
     */
    override suspend fun invoke(
        url: String,
        options: ValidateLinkUseCase.ValidatorOptions,
        reporter: ValidatorReporter,
        visitedUrls: MutableList<String>,
        linkValidator: ValidateLinkUseCase?
    ) {
        val absoluteUrl = URI(url).toURL()

        try {
            val text = httpClient.verifyMimeTypeAndGetBodyAsText(
                url = url,
                acceptableMimeTypes = listOf(RespectAppManifest.MIME_TYPE),
                reporter = reporter
            )

            val respectAppManifest: RespectAppManifest = json.decodeFromString(text)

            respectAppManifest.name.toStringMap().forEach { (_, value) ->
                if (value.isBlank() || value.length > TITLE_MAX_CHARS) {
                    reporter.addMessage(
                        ValidatorMessage(
                            level = ValidatorMessage.Level.ERROR,
                            sourceUri = absoluteUrl.toString(),
                            message = "title \"$value\" invalid length: not between 1 and $TITLE_MAX_CHARS chars"
                        )
                    )
                }
            }

            respectAppManifest.description?.toStringMap()?.forEach { (_, value) ->
                if(value.isBlank() || value.length > DESCRIPTION_MAX_CHARS) {
                    reporter.addMessage(
                        ValidatorMessage(
                            level = ValidatorMessage.Level.ERROR,
                            sourceUri = absoluteUrl.toString(),
                            message = "description \"$value\" invalid length: not between 1 and $DESCRIPTION_MAX_CHARS chars"
                        )
                    )
                    reporter.addMessage(
                        ValidatorMessage(
                            level = ValidatorMessage.Level.ERROR,
                            sourceUri = absoluteUrl.toString(),
                            message = "description \"$value\" invalid length: not between 1 and $DESCRIPTION_MAX_CHARS chars"
                        )
                    )
                }
            }

            val license = respectAppManifest.license
            val allLicenses: SpdxLicenseList = json.decodeFromString(
                this::class.java.getResourceAsStream(
                    "/world/respect/domain/validator/licenses.json"
                )!!.bufferedReader().readText()
            )

            if (license != LICENSE_PROPRIETARY && !allLicenses.licenses.any { it.licenseId == license }) {
                reporter.addMessage(
                    ValidatorMessage(
                        level = ValidatorMessage.Level.ERROR,
                        sourceUri = absoluteUrl.toString(),
                        message = "Invalid license: $license"
                    )
                )
            }

            val websiteVal = respectAppManifest.website
            if (websiteVal != null) {
                validateHttpResponseForUrlUseCase(
                    url = websiteVal.toString(), referer = url, reporter
                )
            } else {
                reporter.addMessage(
                    ValidatorMessage(
                        level = ValidatorMessage.Level.ERROR,
                        sourceUri = absoluteUrl.toString(),
                        message = "website is required"
                    )
                )
            }

            val icon = respectAppManifest.icon ?: getFavIconUseCase(
                Url(absoluteUrl.toString())
            ).firstOrNull {
                it.type in ACCEPTABLE_ICON_FORMATS && ((it.width ?: 0) >= ICON_REQUIRED_SIZE) &&
                        ((it.height ?: 0) >= ICON_REQUIRED_SIZE)
            }

            if (icon == null) {
                reporter.addMessage(
                    ValidatorMessage(
                        level = ValidatorMessage.Level.ERROR,
                        sourceUri = absoluteUrl.toString(),
                        message = "No acceptable icon (webp or png) with resolution >= 512 pixels found. " +
                                "If website does not have an acceptable favicon, must be explicitly specified"
                    )
                )
            }

            validateHttpResponseForUrlUseCase(
                url = respectAppManifest.learningUnits.toString(),
                referer = url,
                reporter = reporter,
                options = ValidateHttpResponseForUrlUseCase.ValidationOptions(
                    acceptableMimeTypes = listOf("application/json", OpdsFeed.MEDIA_TYPE)
                )
            )

            respectAppManifest.android?.packageId?.also { packageId ->
                if (packageId.any { it !in PACKAGE_ID_ALLOWED_CHARS }) {
                    reporter.addMessage(
                        ValidatorMessage(
                            level = ValidatorMessage.Level.ERROR,
                            sourceUri = absoluteUrl.toString(),
                            message = "Invalid packageId: $packageId"
                        )
                    )
                }
            }

            linkValidator?.takeIf { options.followLinks }?.invoke(
                link = ReadiumLink(
                    href = respectAppManifest.learningUnits.toString(),
                    type = OpdsFeed.MEDIA_TYPE,
                ),
                refererUrl = absoluteUrl.toString(),
                options = options,
                reporter = reporter,
                visitedUrls = visitedUrls,
            )
        } catch (e: Throwable) {
            reporter.addMessage(ValidatorMessage.fromException(absoluteUrl.toString(), e))
        }
    }


    companion object {

        val PACKAGE_ID_ALLOWED_CHARS = ('a' .. 'z').plus('A'..'Z')
            .plus('0'..'9').plus("._$".asIterable())

        val ACCEPTABLE_ICON_FORMATS = listOf("image/png", "image/webp")

        const val ICON_REQUIRED_SIZE = 512

        const val TITLE_MAX_CHARS = 80

        const val DESCRIPTION_MAX_CHARS = 4000

        const val LICENSE_PROPRIETARY = "proprietary"



    }

}