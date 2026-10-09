package world.respect.datalayer.school.opds.ext

import io.ktor.http.Url
import world.respect.lib.opds.model.ReadiumLink
import world.respect.lib.opds.model.ext.hasRel
import kotlin.collections.plus

/**
 * Make sure that the list of ReadiumLink has a self link with the given absolute URL.
 *
 * We need the absolute URL in the model after it has been loaded for Playlist editing (when the
 * DataLoadState metadata is no longer available) so that we can store it and use the URL as a key.
 *
 * See also: dataLoadMetaInfoForPlaylist
 */
fun List<ReadiumLink>.withAbsoluteSelfLink(
    urlLoaded: Url
): List<ReadiumLink> {
    return if(any { it.hasRel("self") }) {
        map { link ->
            if(link.hasRel("self")) {
                link.copy(href = urlLoaded.toString())
            }else {
                link
            }
        }
    }else {
        this + ReadiumLink(
            href = urlLoaded.toString(),
            rel = listOf("self"),
            type = "application/opds+json",
        )
    }
}
