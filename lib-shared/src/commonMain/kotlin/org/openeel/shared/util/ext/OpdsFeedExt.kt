package world.respect.shared.util.ext

import io.ktor.http.Url
import world.respect.lib.opds.model.OpdsFeed

/**
 * Resolves all relative URLs (navigation, images, groups, alternates)
 * inside an OpdsFeed against the given base URL.
 */
fun OpdsFeed.resolve(baseUrl: Url): OpdsFeed {
    return this.copy(
        navigation = navigation?.resolveAll(baseUrl),
        publications = publications?.map { it.resolve(baseUrl) },
        groups = groups?.map { group ->
            group.copy(
                publications = group.publications?.map { it.resolve(baseUrl) },
                navigation = group.navigation?.resolveAll(baseUrl),
            )
        },
    )
}
