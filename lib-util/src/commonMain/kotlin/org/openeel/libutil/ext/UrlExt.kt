package world.respect.libutil.ext

import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.appendPathSegments
import io.ktor.http.encodedPath
import io.ktor.http.toURI
import net.thauvin.erik.urlencoder.UrlEncoderUtil


/**
 * Constant for the school url link segment. See schoolUrlOrNull for details.
 */
const val RESPECT_SCHOOL_LINK_SEGMENT = "respect_school_link"

/**
 * Constant for the school url link divider. See schoolUrlOrNull for details.
 */
const val RESPECT_SCHOOL_LINK_DIVIDER = "/$RESPECT_SCHOOL_LINK_SEGMENT/"


/**
 * Resolve an href (that could be absolute or relative)
 *
 * @receiver a KTOR Url (an absolute URL)
 */
fun Url.resolve(href: String): Url {
    return Url(toURI().resolve(href).toString())
}

/**
 * API endpoints may or may not include a trailing / e.g. https://school.example.org/api/school/xapi .
 * If we want to get the URL for a specific endpoint resource (e.g. statements), we may need to add
 * a / and then the path segments
 *
 * This function thus simplifies getting a URL for a specific endpoint resource.
 *
 * @receiver URLBuilder for an endpoint URL that may, or may not, contain a trailing slash
 * @param segments to be added as per appendPathSegments
 * @return URLBuilder with the segments appended
 */
fun URLBuilder.appendEndpointPathSegments(segments: List<String>): URLBuilder {
    if(!encodedPath.endsWith("/") && !segments.first().startsWith("/"))
        appendPathSegments("/")

    appendPathSegments(segments)

    return this
}

/**
 * API endpoints may or may not include a trailing / e.g. https://school.example.org/api/school/xapi .
 * If we want to get the URL for a specific endpoint resource (e.g. statements), we may need to add
 * a / and then the path segments
 *
 * This function thus simplifies getting a URL for a specific endpoint resource.
 *
 * e.g.
 * Url("https://school.example.org/api/school/xapi").appendPathSegments(listOf("statements))
 * returns : "https://school.example.org/api/school/xapi/statements"
 *
 * @return the Url with endpoint segments appended
 */
fun Url.appendEndpointSegments(segments: List<String>): Url {
    return URLBuilder(this).appendEndpointPathSegments(segments).build()
}

/**
 * API endpoints may or may not include a trailing / e.g. https://school.example.org/api/school/xapi .
 * If we want to get the URL for a specific endpoint resource (e.g. statements), we may need to add
 * a / and then the path segments
 *
 * This function thus simplifies getting a URL for a specific endpoint resource.
 *
 * e.g.
 * Url("https://school.example.org/api/school/xapi").appendPathSegments("statements")
 * returns : "https://school.example.org/api/school/xapi/statements"
 *
 * @return the Url with endpoint segments appended
 */
fun Url.appendEndpointSegments(vararg segments: String): Url {
    return appendEndpointSegments(segments.toList())
}

/**
 * Sanitize a URL for use in a filename (e.g. a database name). Replaces anything that is not
 * a letter, digit, _, or - with an underscore
 */
fun Url.sanitizedForFilename(): String {
    return toString().map {
        if(it.isLetter() || it.isDigit() || it == '_' || it == '-') {
            it
        }else {
            '_'
        }
    }.toCharArray().concatToString()
}

/**
 * There are a few different links that the RESPECT app needs to support:
 *
 * QR Code Badges
 * Invite links
 * Playlists
 *
 * When opening a school-specific link the app needs to be able to determine what is the base school
 * url so it can determine what school endpoint it should try to connect to.
 *
 * Hence all links must follow the pattern:
 *
 * https://school.example.org/respect_school_link/linkdetail?param=value
 *
 * School URLs can contain a subpath e.g.
 * https://school.example.org/subpath/respect_school_link/linkdetail?param=value
 *
 * @return The Url for the School as per SchoolDirectoryEntry.self or null if the divider is not
 *         present
 */
fun Url.schoolUrlOrNull(): Url? {
    val urlStr = toString()
    val schoolUrl = urlStr.substringBefore(RESPECT_SCHOOL_LINK_DIVIDER, "")
    return if(schoolUrl == "") {
        null
    }else {
        Url(schoolUrl.requirePostfix("/"))
    }
}

/**
 * As per schoolUrlOrNull, but will throw an exception if the divider is not present.
 *
 * @return The Url for the School as per SchoolDirectoryEntry.self
 */
fun Url.requireSchoolUrl(): Url = schoolUrlOrNull()
    ?: throw IllegalArgumentException("Not a valid school URL: does not contain divider '$RESPECT_SCHOOL_LINK_DIVIDER'")

/**
 * Where the given URL is an endpoint, we normalize it to:
 *
 * a) convert the host into lower case
 * b) require a trailing slash
 *
 * This is necessary because there are instances where we use the URL as a key e.g. in database
 * lookups to find configuration.
 *
 * This does not handle any use of ../ ./ etc.
 */
fun Url.normalizeForEndpoint(): Url {
    return URLBuilder(this).apply {
        host = host.lowercase()

        normalizeForEndpoint()
    }.build()
}

/**
 * When used as part of a URLBuilder, this should be called last, after making any changes to path
 * segments.
 */
fun URLBuilder.normalizeForEndpoint(): URLBuilder {
    return if(pathSegments.lastOrNull()?.endsWith("/") != true) {
        appendPathSegments("/")
    }else {
        this
    }
}


/**
 * Append an assignment id to the embedded xAPI endpoint.
 */
fun Url.appendAssignmentXapiSegment(
    activityId: String,
) : Url {
    return appendEndpointSegments(
        //As per OpenEeelXapiConstants.ASSIGNMENT_XAPI_SEGMENT
        "openeel_assignment",
        //Required to be double encoded as per comments on XapiNanoHttpdApp
        UrlEncoderUtil.encode(activityId),
    )
}
