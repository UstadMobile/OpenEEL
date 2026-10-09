package world.respect.domain.validator

/**
 * See: https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Link
 *
 * Used to look for and parse link headers on http response (required to discover a manifest)
 */
class HttpLinkHeader(
    val links: List<Link>
) {

    data class Link(
        val uriRef: String,
        val params: Map<String, String>
    )

    companion object {

        fun parseHeaderValue(string: String): HttpLinkHeader {
            return HttpLinkHeader(
                links = string.split(",").map { linkStr ->
                    Link(
                        uriRef = linkStr.substringAfter("<").substringBefore(">"),
                        params = linkStr.substringAfter(">").substringAfter(";")
                            .split(";").associate { paramPart ->
                                val paramName = paramPart.substringBefore("=").trim()
                                val paramValuePart = paramPart.substringAfter("=")
                                val paramValue = if (paramValuePart.startsWith("\"")) {
                                    //Quoted
                                    paramValuePart.trim().removeSurrounding("\"")
                                } else {
                                    paramValuePart.trim()
                                }

                                paramName to paramValue
                            }
                    )
                }
            )
        }

    }
}