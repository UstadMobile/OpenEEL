package world.respect.shared.domain.opds.validator

import io.ktor.http.Url
import io.ktor.util.toMap
import org.jsoup.Jsoup
import world.respect.lib.opds.model.Publication
import world.respect.lib.opds.model.ReadiumLink
import world.respect.lib.opds.model.toStringMap
import world.respect.datalayer.respect.model.LEARNING_UNIT_MIME_TYPES
import world.respect.domain.validator.HttpLinkHeader
import world.respect.shared.domain.validator.ValidateHttpResponseForUrlUseCase
import world.respect.shared.domain.validator.ValidatorMessage
import world.respect.shared.domain.validator.ValidatorReporter
import java.net.URI

/**
 * Validate an Opds Publication Element.
 *
 * This use case operates an OpdsPublication instance and is therefor used by both the
 * OpdsFeedValidator and the OpdsPublicationValidator.
 */
class ValidateOpdsPublicationUseCase(
    private val validateHttpResponseForUrlUseCase: ValidateHttpResponseForUrlUseCase,
) {

    /**
     * @property discoveredManifestLinksToValidate Lists of Manifests that were discovered using the
     * discovery method as per the Readium spec, unless the URL discovered is the same as the one
     * already being validated.
     */
    data class ValidateOpdsPublicationUseCaseResult(
        val discoveredManifestLinksToValidate: List<ReadiumLink>
    )

    /**
     * @param publication OpdsPublication to be validated
     * @param url the URL of the OPDS JSON loaded (used to resolve links)
     * @param reporter ValidatorReporter
     *
     * @return ValidateOpdsPublicationUseCaseResult contains a list of discovered manifests to validate
     */
    suspend operator fun invoke(
        publication: Publication,
        url: String,
        reporter: ValidatorReporter,
    ): ValidateOpdsPublicationUseCaseResult {
        val discoveredManifestLinksToValidate = mutableListOf<ReadiumLink>()
        val acquisitionLinks = publication.links.filter { link ->
            val linkType = link.type?.substringBefore(";")
            link.rel?.any { it.startsWith("http://opds-spec.org/acquisition") } == true &&
                    linkType in LEARNING_UNIT_MIME_TYPES
        }

        val publicationTitleAndId = buildString {
            append("title \"")
            append(publication.metadata.title.toStringMap().values.firstOrNull() ?: "[Unknown]")
            append("\"")
            publication.metadata.identifier?.also {
                append(" and identifier: $it")
            }
        }

        reporter.addMessage(
            ValidatorMessage(
                ValidatorMessage.Level.DEBUG,
                sourceUri = url,
                message = "Validating Opds Publication: $publicationTitleAndId"
            )
        )


        //Check publication has acceptable acquisition link element
        if(acquisitionLinks.isEmpty()) {
            reporter.addMessage(
                ValidatorMessage(
                    level = ValidatorMessage.Level.ERROR,
                    sourceUri = url,
                    message = buildString {
                        append("No suitable acquisition links found for publication: ")
                        append(publicationTitleAndId)
                        append(". Acquisition link MUST use rel starting with http://opds-spec.org/acquisition " +
                                "AND use one of the following types: ${LEARNING_UNIT_MIME_TYPES.joinToString()}")
                    }
                )
            )
        }

        val imageLinks = publication.images
        if(imageLinks.isNullOrEmpty()) {
            reporter.addMessage(
                ValidatorMessage(
                    level = ValidatorMessage.Level.ERROR,
                    sourceUri = url,
                    message = buildString {
                        append("No images found for publication: ")
                        append(publicationTitleAndId)
                        append(". Publication MUST contain at least one image")
                    }
                )
            )
        }

        imageLinks?.forEach {
            validateHttpResponseForUrlUseCase(
                url = URI(url).resolve(it.href).toString(),
                referer = url,
                reporter = reporter,
            )
        }

        //Check learning resource id (URL) does not contain a # (that would not go to server) or
        //use any reserved query parameters
        acquisitionLinks.forEach { link ->
            val learningResourceIdUrl = URI(url).resolve(link.href)
            if(learningResourceIdUrl.toString().contains("#")) {
                reporter.addMessage(
                    ValidatorMessage(
                        level = ValidatorMessage.Level.ERROR,
                        sourceUri = url,
                        message = buildString {
                            append("Learning Resource ID URL ($learningResourceIdUrl) contains a #" )
                            append(publicationTitleAndId)
                        }
                    )
                )
            }

            val queryParamNames = Url((learningResourceIdUrl.toString())).parameters.toMap().keys
            val reservedQueryParamNamesUsed = queryParamNames.filter {
                it in LEARNING_UNIT_RESERVED_PARAMS
            }
            if(reservedQueryParamNamesUsed.isNotEmpty()) {
                reporter.addMessage(
                    ValidatorMessage(
                        level = ValidatorMessage.Level.ERROR,
                        sourceUri = url,
                        message = buildString {
                            append("Learning Resource ID URL ($learningResourceIdUrl) for publication ")
                            append(publicationTitleAndId)
                            append(" contains reserved query parameters: ${reservedQueryParamNamesUsed.joinToString()}")
                        }
                    )
                )
            }

            //Check learning resource URL results in an HTTP 200 OK response with acceptable mime type
            val responseValidationResult = validateHttpResponseForUrlUseCase(
                url = learningResourceIdUrl.toString(),
                referer = url,
                reporter = reporter,
                options = ValidateHttpResponseForUrlUseCase.ValidationOptions(
                    acceptableMimeTypes = LEARNING_UNIT_MIME_TYPES
                )
            )

            //Try to discover manifest link as per the Readium publication manifest specification
            val manifestHeaderLink = responseValidationResult.responseHeaders?.getAll("Link")
                ?.firstNotNullOf { headerVal ->
                    HttpLinkHeader.parseHeaderValue(headerVal).links.firstOrNull {
                        it.params["rel"]?.contains("manifest") == true &&
                            it.params["type"]?.substringBefore(";")?.trim() == "application/webpub+json"
                    }
                }

            val jsoupDoc = try {
                Jsoup.connect(learningResourceIdUrl.toString()).get()
            }catch(e: Throwable) {
                reporter.addMessage(
                    ValidatorMessage(
                        level = ValidatorMessage.Level.ERROR,
                        sourceUri = url,
                        message = buildString {
                            append("Could not load or parse learning resource ID: $learningResourceIdUrl: ${e.message}")
                            append(" URL MUST return an HTTP 200/OK response and MUST contain valid HTML or XHTML")
                        }
                    )
                )
                null
            }

            val manifestUrl = manifestHeaderLink?.uriRef?.let {
                learningResourceIdUrl.resolve(URI(it)).toString()
            } ?: jsoupDoc?.select("link")?.firstOrNull { node ->
                node.attr("rel")
                    .ifEmpty { null }
                    ?.split(Regex("\\W"))
                    ?.contains("manifest") == true &&
                        node.attr("type")
                            .ifEmpty { null }
                            ?.substringBefore(";")
                            ?.trim() == "application/webpub+json"
            }?.absUrl("href")

            if(manifestUrl == null) {
                reporter.addMessage(
                    ValidatorMessage(
                        level = ValidatorMessage.Level.ERROR,
                        sourceUri = url,
                        message = buildString {
                            append("Manifest not discovered for learning resource ID URL: $learningResourceIdUrl .")
                            append("Readium publication manifest must be discoverable using a link")
                            append("tag in HTML or Link header for the learning resource ID URL as per ")
                            append("https://github.com/readium/webpub-manifest?tab=readme-ov-file#5-discovering-a-manifest")
                        }
                    )
                )
            }else {
                /* If this is the manifest URL link discovered, then check for resources.
                 * Otherwise add to the list of discovered manifest links
                 */
                if(manifestUrl == url) {
                    if(publication.resources.isNullOrEmpty()) {
                        reporter.addMessage(
                            ValidatorMessage(
                                level = ValidatorMessage.Level.ERROR,
                                message = """The manifest which is discovered using the
                                    |discovery process as per the Readium spec ( https://github.com/readium/webpub-manifest?tab=readme-ov-file#5-discovering-a-manifest )
                                    |MUST contain a list of all resources required.
                                """.trimMargin(),
                                sourceUri = url
                            )
                        )
                    }

                    publication.resources?.forEach { resource ->
                        val resourceUrl = learningResourceIdUrl.resolve(URI(resource.href)).toString()
                        validateHttpResponseForUrlUseCase(
                            url = resourceUrl,
                            referer = url,
                            reporter = reporter,
                        )
                    }
                }else {
                    discoveredManifestLinksToValidate.add(
                        ReadiumLink(
                            href = manifestUrl,
                            type = "application/webpub+json"
                        )
                    )
                }
            }
        }

        return ValidateOpdsPublicationUseCaseResult(
            discoveredManifestLinksToValidate = discoveredManifestLinksToValidate,
        )
    }

    companion object {
        val LEARNING_UNIT_RESERVED_PARAMS = listOf(
            "respectLaunchVersion", "auth", "given_name", "locale", "endpoint",
            "actor", "registration", "activity_id"
        )

    }

}