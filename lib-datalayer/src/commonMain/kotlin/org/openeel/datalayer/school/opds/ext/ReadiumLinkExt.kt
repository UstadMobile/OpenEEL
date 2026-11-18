package org.openeel.datalayer.school.opds.ext

import io.ktor.http.Url
import org.openeel.lib.opds.model.ReadiumLink
import org.openeel.lib.opds.model.ext.hasRel
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

/**
 * Find icons for a [ReadiumLink]. This isn't really defined so well externally (e.g. publications
 * have a dedicated images collection), so here we are looking for a link in the alternate sub
 * element with the rel "icon".
 */
fun ReadiumLink.findIcons(): List<ReadiumLink> {
    return this.alternate.orEmpty().filter { it.hasRel("icon") }
}
