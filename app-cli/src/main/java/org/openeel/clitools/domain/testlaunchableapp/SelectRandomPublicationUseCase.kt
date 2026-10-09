package world.respect.clitools.domain.testlaunchableapp

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.opds.model.Publication
import world.respect.lib.opds.model.ReadiumLink
import world.respect.lib.opds.model.ext.allPublications
import world.respect.lib.opds.model.findSelfLinks
import world.respect.libutil.ext.resolve
import world.respect.shared.ext.selectPreferredString
import kotlin.random.Random

/**
 * Selects a publication at random by following links in an OpdsFeed until reaching a publication.
 *
 * Open the OpdsFeed.
 *    If the OpdsFeed contains only navigation items, select a random navigation
 *    item, follow the link, and repeat.
 *
 *    If the OpdsFeed contains publications, select a random publication and return it as the result
 *
 */
class SelectRandomPublicationUseCase(
    private val httpClient: HttpClient,
    private val json: Json,
    private val random: Random = Random.Default,
) {

    /**
     * @property opdsFeedUrl the OpdsFeed URL to open
     */
    data class Request(
        val opdsFeedUrl: Url,
        val maxDepth: Int = MAX_DEPTH,
    )

    /**
     * A step in getting from the request to the randomly selected publication.
     *
     * @property text the text that needs to be clicked (eg title)
     * @property feed OpdsFeed that needs to be opened
     */
    data class ClickStep(
        val text: String,
        val feed: OpdsFeed?,
        val link: ReadiumLink,
    )

    /**
     * @property publication the publication selected at random.
     * @property clickPath the sequence that goes from the Feed in [Request.opdsFeedUrl] to the
     *           [publication].
     *
     */
    data class Result(
        val publication: Publication,
        val clickPath: List<ClickStep>,
    )

    suspend operator fun invoke(
        request: Request,
    ): Result {
        var currentUrl = request.opdsFeedUrl
        var clickCount = 0
        val clickPath = mutableListOf<ClickStep>()

        while (clickCount < request.maxDepth) {
            val responseText = httpClient.get(currentUrl).bodyAsText()
            val feed = json.decodeFromString<OpdsFeed>(responseText)

            val publications = feed.allPublications()
            if (publications.isNotEmpty()) {
                val selectedPublication = publications.random(random)
                clickPath.add(
                    ClickStep(
                        text = selectedPublication.metadata.title.selectPreferredString(listOf("en")),
                        link = ReadiumLink(
                            href = currentUrl.resolve(
                                selectedPublication.findSelfLinks().first().href
                            ).toString()
                        ),
                        feed = null,
                    )
                )

                return Result(
                    publication = selectedPublication,
                    clickPath = clickPath.toList(),
                )
            }

            val navigationItems = feed.navigation.orEmpty() + feed.groups.orEmpty().flatMap { it.navigation.orEmpty() }
            if (navigationItems.isNotEmpty()) {
                val selectedNavItem = navigationItems.random(random)
                clickPath.add(
                    ClickStep(
                        text = selectedNavItem.title ?: "",
                        feed = feed,
                        link = selectedNavItem,
                    )
                )
                currentUrl = currentUrl.resolve(selectedNavItem.href)
            } else {
                throw IllegalStateException("Feed at $currentUrl contains neither publications nor navigation items")
            }
            clickCount++
        }

        throw IllegalStateException("Exceeded max depth (${request.maxDepth}): $clickCount steps and no publication found")
    }

    companion object {

        const val MAX_DEPTH = 10

    }

}