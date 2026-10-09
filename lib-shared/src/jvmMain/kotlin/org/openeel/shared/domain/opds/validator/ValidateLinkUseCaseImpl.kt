package world.respect.shared.domain.opds.validator

import io.ktor.http.isSuccess
import world.respect.shared.domain.respectappmanifest.validator.RespectAppManifestValidator
import world.respect.datalayer.compatibleapps.model.RespectAppManifest
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.opds.model.Publication
import world.respect.lib.opds.model.ReadiumLink
import world.respect.shared.domain.validator.ValidateHttpResponseForUrlUseCase
import world.respect.domain.validator.ValidateLinkUseCase
import world.respect.shared.domain.validator.ValidatorMessage
import world.respect.shared.domain.validator.ValidatorReporter
import java.net.URI

class ValidateLinkUseCaseImpl(
    private val opdsFeedValidator: OpdsFeedValidator,
    private val opdsPublicationValidator: OpdsPublicationValidator,
    private val respectAppManifestValidator: RespectAppManifestValidator,
    private val validateHttpResponseForUrlUseCase: ValidateHttpResponseForUrlUseCase,
) : ValidateLinkUseCase {

    override suspend operator fun invoke(
        link: ReadiumLink,
        refererUrl: String,
        options: ValidateLinkUseCase.ValidatorOptions,
        reporter: ValidatorReporter,
        visitedUrls: MutableList<String>,
    ) {
        val linkType = link.type ?: OpdsFeed.MEDIA_TYPE

        val baseUrlUri = URI(refererUrl)
        val linkUrl = baseUrlUri.resolve(link.href).toURL().toString()

        if(linkUrl in visitedUrls) {
            reporter.addMessage(
                ValidatorMessage(
                    level = ValidatorMessage.Level.DEBUG,
                    sourceUri = refererUrl,
                    message = "Skipping link as it has already been visited: $linkUrl"
                )
            )
            return
        }

        visitedUrls.add(linkUrl)

        val httpResponseResult = validateHttpResponseForUrlUseCase(
            url = linkUrl,
            referer = refererUrl,
            reporter = reporter,
            options = if(options.skipRespectChecks) {
                ValidateHttpResponseForUrlUseCase.ONLY_CHECK_RESPONSE_IS_SUCCESS
            }else {
                ValidateHttpResponseForUrlUseCase.DEFAULT_VALIDATION_OPTS
            },
        )

        if(httpResponseResult.statusCode?.isSuccess() == false) {
            reporter.addMessage(
                ValidatorMessage(
                    level = ValidatorMessage.Level.DEBUG,
                    sourceUri = refererUrl,
                    message = "ValidateLinkUseCase: Skipping validator run for link to $linkUrl as " +
                            "it did not return a successful status code (${httpResponseResult.statusCode}): " +
                            linkUrl
                )
            )

            return
        }

        val validatorToRun = when(linkType) {
            OpdsFeed.MEDIA_TYPE -> {
                opdsFeedValidator
            }
            Publication.MEDIA_TYPE, Publication.MEDIA_TYPE_READIUM_MANIFEST -> {
                opdsPublicationValidator
            }
            RespectAppManifest.MIME_TYPE -> {
                respectAppManifestValidator
            }
            else -> null
        }

        validatorToRun?.invoke(
            url = linkUrl,
            options = options,
            reporter = reporter,
            visitedUrls = visitedUrls,
            linkValidator = if(options.followLinks) this else null,
        )
    }

}