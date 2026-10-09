package world.respect.shared.domain.externallink

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.isSuccess
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import world.respect.libutil.ext.resolve

/**
 * Extracts the title, description, and preview image for a web page so that a preview can be
 * shown when a user adds an external link.
 *
 * Metadata is taken from the Open Graph tags contained in the HTML of the HTTP response, as per
 * the Open Graph protocol: https://ogp.me/ . These tags are part of the document served by the
 * origin server, so no Javascript execution (and therefore no WebView) is required.
 * Where a page provides no Open Graph tags, the standard HTML title element and description meta
 * tag are used as a fallback:
 * https://developer.mozilla.org/en-US/docs/Web/HTML/Element/meta/name
 */
class ExtractWebPageMetadataUseCaseAndroid(
    private val httpClient: HttpClient,
) : ExtractWebPageMetadataUseCase {

    /**
     * @param url the URL of the page to extract metadata from.
     * @return the metadata found for the page. Individual properties are null where the page does
     *         not provide them.
     * @throws IllegalStateException where the page could not be retrieved.
     */
    override suspend fun invoke(url: String): WebPageMetadata {
        val response = httpClient.get(url)

        if (!response.status.isSuccess()) {
            throw IllegalStateException(
                "Cannot extract metadata: HTTP ${response.status.value} for $url"
            )
        }

        /*
         * The URL of the request as sent, which is the final URL after any redirects have been
         * followed. Used as the base against which relative URLs are resolved.
         */
        val responseUrl = response.request.url

        val document = Jsoup.parse(response.bodyAsText(), responseUrl.toString())

        return WebPageMetadata(
            title = document.metaContent(OG_TITLE)
                ?: document.title().takeIf { it.isNotBlank() },
            description = document.metaContent(OG_DESCRIPTION)
                ?: document.metaContent(META_DESCRIPTION),
            imageUrl = document.metaContent(OG_IMAGE)?.let { imageUrl ->
                /*
                 * The Open Graph protocol requires og:image to be an absolute URL, however
                 * relative URLs are used in practice, so resolve against the page URL. Resolving
                 * a URL that is already absolute returns that URL unchanged.
                 */
                responseUrl.resolve(imageUrl).toString()
            },
        )
    }

    /**
     * Finds the content of a meta tag.
     *
     * The Open Graph protocol specifies that tags are identified using the property attribute,
     * however some sites use the name attribute instead, so both are accepted. Standard (non Open
     * Graph) meta tags such as description always use the name attribute.
     *
     * @param key the value of the property or name attribute to look for e.g. og:title
     * @return the content of the first matching meta tag, or null where there is no such tag or
     *         its content is blank.
     */
    private fun Document.metaContent(key: String): String? {
        return getElementsByTag(TAG_META).firstOrNull { metaTag ->
            metaTag.attr(ATTR_PROPERTY) == key || metaTag.attr(ATTR_NAME) == key
        }?.attr(ATTR_CONTENT)?.takeIf { it.isNotBlank() }
    }

    companion object {

        /* Open Graph properties as per https://ogp.me/ */
        private const val OG_TITLE = "og:title"
        private const val OG_DESCRIPTION = "og:description"
        private const val OG_IMAGE = "og:image"

        /*
         * HTML meta tag and attribute names as per
         * https://developer.mozilla.org/en-US/docs/Web/HTML/Element/meta
         */
        private const val TAG_META = "meta"
        private const val META_DESCRIPTION = "description"
        private const val ATTR_PROPERTY = "property"
        private const val ATTR_NAME = "name"
        private const val ATTR_CONTENT = "content"
    }
}