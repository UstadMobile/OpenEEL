package world.respect.shared.domain.opds.validator

import com.networknt.schema.InputFormat
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.opds.model.Publication
import world.respect.lib.opds.model.ReadiumLink
import world.respect.domain.opds.validator.verifyMimeTypeAndGetBodyAsText
import world.respect.domain.validator.ValidateLinkUseCase
import world.respect.shared.domain.validator.ValidatorMessage
import world.respect.shared.domain.validator.ValidatorReporter

/**
 * Validate on OPDS Feed
 */
class OpdsFeedValidator(
    private val json: Json,
    private val httpClient: HttpClient,
    private val validateOpdsPublicationUseCase: ValidateOpdsPublicationUseCase,
) : AbstractJsonSchemaValidator(
    schemaUrl = "https://drafts.opds.io/schema/feed.schema.json"
) {

    override suspend operator fun invoke(
        url: String,
        options: ValidateLinkUseCase.ValidatorOptions,
        reporter: ValidatorReporter,
        visitedUrls: MutableList<String>,
        linkValidator: ValidateLinkUseCase?,
    ) {
        val discoveredManifests = mutableListOf<ReadiumLink>()
        try {
            val text = httpClient.verifyMimeTypeAndGetBodyAsText(
                url = url,
                acceptableMimeTypes = listOf(Publication.MEDIA_TYPE, "application/json"),
                reporter = reporter
            )

            val messages = schema.validate(text, InputFormat.JSON)
            messages.forEach {
                reporter.addMessage(it.toValidatorMessage(sourceUri = url))
            }

            val opdsFeed = json.decodeFromString<OpdsFeed>(text)

            val allPublications = (opdsFeed.publications ?: emptyList()) +
                    (opdsFeed.groups?.flatMap { it.publications ?: emptyList() } ?: emptyList())

            if(!options.skipRespectChecks) {
                allPublications.forEach {
                    val publicationValidation = validateOpdsPublicationUseCase(it, url, reporter)
                    discoveredManifests.addAll(publicationValidation.discoveredManifestLinksToValidate)
                }
            }

            val allNavigation = (opdsFeed.navigation ?: emptyList()) +
                    (opdsFeed.groups?.flatMap { it.navigation ?: emptyList() } ?: emptyList())

            /*
             * How to set links for a navigation feed is not settled as per the OPDS spec. See
             * https://github.com/opds-community/drafts/issues/64
             */
            val navigationIconLinks = allNavigation.mapNotNull { navLink ->
                val iconLink = navLink.alternate?.firstOrNull {
                    it.rel?.contains("icon") == true
                }

                if(!options.skipRespectChecks && iconLink == null) {
                    reporter.addMessage(
                        ValidatorMessage(
                            level = ValidatorMessage.Level.WARN,
                            sourceUri = url,
                            message = buildString {
                                append("Navigation link to ${navLink.href} SHOULD contain an icon")
                            }
                        )
                    )
                }

                iconLink
            }

            if(linkValidator != null) {
                val allLinks = opdsFeed.links + (opdsFeed.navigation ?: emptyList()) +
                        (opdsFeed.facets?.flatMap { it.links } ?: emptyList() ?: emptyList()) +
                        (opdsFeed.groups?.flatMap { it.links ?: emptyList() } ?: emptyList()) +
                        (opdsFeed.publications?.flatMap { it.links } ?: emptyList()) +
                        navigationIconLinks

                (allLinks + discoveredManifests).forEach { link ->
                    linkValidator(link, url, options, reporter, visitedUrls)
                }
            }
        }catch(e : Throwable) {
            reporter.addMessage(ValidatorMessage.fromException(url, e))
        }
    }


}