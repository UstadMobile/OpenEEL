package org.openeel.shared.util.ext

import io.ktor.http.Url
import org.openeel.lib.opds.model.Publication
import org.openeel.lib.opds.model.ReadiumLink
import org.openeel.libutil.ext.resolve
import org.openeel.shared.util.ext.resolve
import org.openeel.shared.viewmodel.catalog.PublicationsSelection

fun Publication.resolve(baseUrl: Url) : Publication {
    return copy(
        links = links.resolveAll(baseUrl),
        images = images?.resolveAll(baseUrl),
        readingOrder = readingOrder?.resolveAll(baseUrl),
        resources = resources?.resolveAll(baseUrl),
        toc = toc?.resolveAll(baseUrl),
    )
}

fun Publication.key(
    indexInFeed: Int,
) : String {
    return metadata.identifier?.toString() ?: indexInFeed.toString()
}


/**
 * Create an Activity ID for the legacy launch schema where the launcher app would simply look for
 * an acquisition link.
 */
fun Publication.legacyActivityIdForLink(
    link: ReadiumLink,
    publicationUrl: Url,
): String {
    return metadata.identifier?.toString() ?: publicationUrl.resolve(link.href).toString()
}

/**
 * Take an existing list of publications, then add selected publications. This will
 * a) Resolve all links in the selection to absolute links using [PublicationsSelection.url]
 * b) Return a list with the receiver list and the resolved publications
 *
 * @param selection [PublicationsSelection] to add
 * @receiver a List of [Publication]
 * @return List with the receiver list and selected publications resolved.
 */
fun List<Publication>.resolveAndAddSelection(selection: PublicationsSelection) : List<Publication>{
    return buildList {
        addAll(this@resolveAndAddSelection)
        addAll(selection.selectedPublications.map {
            it.resolve(selection.url) }
        )
    }
}
