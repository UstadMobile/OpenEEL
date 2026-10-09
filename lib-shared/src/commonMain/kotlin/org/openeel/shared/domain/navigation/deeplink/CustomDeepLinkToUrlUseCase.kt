package world.respect.shared.domain.navigation.deeplink

import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.encodedPath

/**
 * See UrlToCustomerDeepLinkUseCase
 */
class CustomDeepLinkToUrlUseCase(
    val customProtocol: String,
) {

    operator fun invoke(url: Url): Url {
        if(url.protocol.name != customProtocol)
            return url

        return URLBuilder().apply {
            protocol = URLProtocol.createOrDefault(url.host)
            host = url.segments.first()
            port = url.specifiedPort

            /*
             * The custom deep link path normally looks like /school.example.org/some/path .
             *
             * Just using segments lists directly does not correctly handle situations where the
             * path ends with a slash. The trailing slash is omitted.
             *
             * So:
             * 1: Remove the initial trailing slash (if present)
             * 2: Take everything after the the next slash (e.g. some/path)
             * 3: Always put the prefix slash back in
             */
            encodedPath = "/${url.encodedPath.removePrefix("/").substringAfter("/")}"

            parameters.appendAll(url.parameters)
            fragment = url.fragment
            user = url.user
            password = url.password
            trailingQuery = url.trailingQuery
        }.build()

    }


}