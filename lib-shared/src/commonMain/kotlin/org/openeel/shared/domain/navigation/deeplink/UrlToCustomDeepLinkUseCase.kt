package world.respect.shared.domain.navigation.deeplink

import com.ustadmobile.ihttp.ext.requirePrefix
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.encodedPath

/**
 * RESPECT handles two types of link as per:
 * https://developer.android.com/training/app-links/create-deeplinks

 * App links: verified https URLs that open automatically using the RESPECT app (once verified)
 * Custom deep links: using a custom protocol in the form of package-name:///https/example.org/path
 *
 * Custom deep links: used when using Chrome Custom Tabs for self-sign-up of schools (similar to
 * standard practice when using OAuth). Custom deep links have the advantage of not requiring
 * verification.
 *
 * Conversion is as follows:
 * https://schoolname.example.org/some/path?param=value
 * becomes
 * world.respect.app://https/schoolname.example.org/some/path?param=value
 *
 * http://192.168.1.2:8098/some/path?param=value
 * becomes
 * world.respect.app://192.168.1.2:8098/some/path?param=value
 *
 * The mapping moves the http/https into the host, and the host into the path. We want to have a
 * single custom protocol, however we need to support both http and https links.
 *
 * A Custom Deep Link created using UrlToCustomDeepLinkUseCase can be converted back using
 * CustomDeepLinkToUrlUseCase.
 */
class UrlToCustomDeepLinkUseCase(
    val customProtocol: String,
) {

    operator fun invoke(url : Url): Url {
        if(url.protocol.name == customProtocol)
            return url

        return URLBuilder().apply {
            protocol = URLProtocol.createOrDefault(customProtocol)
            host = url.protocol.name
            port = url.specifiedPort
            encodedPath = "${url.host}${url.encodedPath.requirePrefix("/")}"

            parameters.appendAll(url.parameters)
            fragment = url.fragment
            user = url.user
            password = url.password
            trailingQuery = url.trailingQuery
        }.build()
    }

}