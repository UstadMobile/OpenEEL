package world.respect.domain.validator

import kotlin.test.Test
import kotlin.test.assertEquals

class HttpLinkHeaderTest {

    @Test
    fun givenLinkWithQuotedParams_whenParsed_returnsExpectedValues() {
        val parsed = HttpLinkHeader.parseHeaderValue(
            "<http://example.app/opds/lesson001.json>; rel=\"manifest\"; type=\"application/webpub+json\""
        )
        val firstLink = parsed.links.first()
        assertEquals("http://example.app/opds/lesson001.json", firstLink.uriRef)
        assertEquals("manifest", firstLink.params["rel"])
        assertEquals("application/webpub+json", firstLink.params["type"])
    }

    @Test
    fun givenLinkWithUnquotedParams_whenParsed_returnsExpectedValues() {
        val parsed = HttpLinkHeader.parseHeaderValue(
            "<http://example.app/opds/lesson001.json>; rel=manifest; type=application/webpub+json"
        )
        val firstLink = parsed.links.first()
        assertEquals("http://example.app/opds/lesson001.json", firstLink.uriRef)
        assertEquals("manifest", firstLink.params["rel"])
        assertEquals("application/webpub+json", firstLink.params["type"])
    }

}