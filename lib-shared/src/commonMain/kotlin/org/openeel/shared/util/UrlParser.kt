package world.respect.shared.util

import io.ktor.http.Url

object UrlParser {
    fun extractBadgeNumberFromUrl(url: Url): String? {
        val urlStr = url.toString()
        val idSegment = "/id/"
        if (!urlStr.contains(idSegment)) return null
        val afterId = urlStr.substringAfter(idSegment)
        val badgeId = afterId
            .substringBefore('?')
            .substringBefore('#')
            .substringBefore('/')

        return badgeId.takeIf { it.isNotEmpty() && it.all(Char::isDigit) }
    }
}
