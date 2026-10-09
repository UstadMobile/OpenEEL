package world.respect.server.routes.school.respect

import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.appendEncodedPathSegments
import io.ktor.http.formUrlEncode
import io.ktor.http.parametersOf
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import world.respect.datalayer.respect.model.APPSTORE_REDIRECT_BASE
import world.respect.libutil.ext.RESPECT_SCHOOL_LINK_SEGMENT
import world.respect.server.util.ext.virtualHost
import world.respect.shared.domain.navigation.deferreddeeplink.GetDeferredDeepLinkUseCase

/**
 * Redirect and set the referrer parameter such that it will be picked up by hte
 */
fun Route.SchoolLinkRoute() {
    get("{path...}") {
        val schoolUrl = call.request.virtualHost
        val thisUrl = URLBuilder(schoolUrl).apply {
            appendEncodedPathSegments(RESPECT_SCHOOL_LINK_SEGMENT)
            call.parameters["path"]?.also { appendEncodedPathSegments(it) }
            parameters.appendAll(call.request.queryParameters)
        }.build()

        /**
         * The referrer parameter needs to be in the form of deferredDeepLink=url, where the url
         * itself is URL encoded.
         */
        val redirectToUrl = URLBuilder(APPSTORE_REDIRECT_BASE).apply {
            parameters.append(
                name = "referrer",
                value = parametersOf(
                    GetDeferredDeepLinkUseCase.PARAM_NAME_DEFERRED_DEEP_LINK, thisUrl.toString()
                ).formUrlEncode()
            )
        }

        call.respondText(
            text = """
                <html>
                <head>
                <meta http-equiv="refresh" content="1; url=${redirectToUrl}" />
                </head>
                <body>
                
                <a href="$redirectToUrl">Click to continue</a> to access RESPECT via Google Play 
                if not redirected automatically within 5 seconds
                 </body>
                 </html>
                
            """,
            contentType = ContentType.Text.Html,
        )

        call.respondText(redirectToUrl.toString())
    }
}