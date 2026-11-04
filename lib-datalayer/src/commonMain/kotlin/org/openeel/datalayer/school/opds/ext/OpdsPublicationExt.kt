package org.openeel.datalayer.school.opds.ext

import io.ktor.http.Url
import org.openeel.lib.opds.model.Publication
import org.openeel.lib.opds.model.ext.hasRel

fun Publication.withAbsoluteSelfUrl(urlLoaded: Url): Publication {
    return copy(links = links.withAbsoluteSelfLink(urlLoaded))
}

fun Publication.requireAbsoluteSelfUrl(): Url {
    return Url(
        links.first { it.hasRel("self") }.href
    )
}
