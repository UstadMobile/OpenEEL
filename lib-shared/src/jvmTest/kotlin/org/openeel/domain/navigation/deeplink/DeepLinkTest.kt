package world.respect.domain.navigation.deeplink

import io.ktor.http.Url
import world.respect.shared.domain.navigation.deeplink.CustomDeepLinkToUrlUseCase
import world.respect.shared.domain.navigation.deeplink.UrlToCustomDeepLinkUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

class DeepLinkTest {

    val urlToCustomDeepLinkUseCase = UrlToCustomDeepLinkUseCase(CUSTOM_PROTO)

    val customDeepLinkToUrlUseCase = CustomDeepLinkToUrlUseCase(CUSTOM_PROTO)

    private fun assertWhenConvertedToDeepLinkThenConvertedBackWillMatch(url: Url) {
        val customDeepLink = urlToCustomDeepLinkUseCase(url)
        val convertedBack = customDeepLinkToUrlUseCase(customDeepLink)
        assertEquals(url, convertedBack)
    }

    @Test
    fun givenLinkConvertedToDeepLink_whenConvertedBack_thenShouldMatch() {
        assertWhenConvertedToDeepLinkThenConvertedBackWillMatch(
            Url("https://school.example.org/some/path/?param=value"))
        assertWhenConvertedToDeepLinkThenConvertedBackWillMatch(
            Url("http://localhost:8080/some/path"))
        assertWhenConvertedToDeepLinkThenConvertedBackWillMatch(
            Url("https://school.example.org/some/path?param=value"))

        assertWhenConvertedToDeepLinkThenConvertedBackWillMatch(
            Url("http://192.168.1.3:8098/some/path/"))
        assertWhenConvertedToDeepLinkThenConvertedBackWillMatch(
            Url("https://school.example.org/"))
        assertWhenConvertedToDeepLinkThenConvertedBackWillMatch(
            Url("https://school.example.org:8099/path/"))
        assertWhenConvertedToDeepLinkThenConvertedBackWillMatch(
            Url("https://school.example.org/some/path"))

    }

    companion object {

        const val CUSTOM_PROTO = "world.respect.app"

    }
}