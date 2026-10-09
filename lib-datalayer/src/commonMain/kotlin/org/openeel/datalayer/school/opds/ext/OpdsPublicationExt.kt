package world.respect.datalayer.school.opds.ext

import io.ktor.http.Url
import world.respect.lib.opds.model.Publication
import world.respect.lib.opds.model.ext.hasRel

fun Publication.withAbsoluteSelfUrl(urlLoaded: Url): Publication {
    return copy(links = links.withAbsoluteSelfLink(urlLoaded))
}

fun Publication.requireAbsoluteSelfUrl(): Url {
    return Url(
        links.first { it.hasRel("self") }.href
    )
}
