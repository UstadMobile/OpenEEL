package world.respect.datalayer.school.opds.ext

import io.ktor.http.Url
import world.respect.lib.dataloadstate.DataLoadMetaInfo
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.opds.model.ReadiumLink
import world.respect.libutil.util.time.systemTimeInMillis

fun OpdsFeed.selfUrl(): Url? {
    return links.firstOrNull { "self" in (it.rel ?: emptyList()) }?.let {
        Url(it.href)
    }
}

fun OpdsFeed.requireSelfUrl(): Url {
    return selfUrl() ?: throw IllegalStateException("No self url found")
}

/**
 * Make sure that the OpdsFeed has a self link with the absolute URL.
 *
 * We need the absolute URL in the model after it has been loaded for Playlist editing (when the
 * DataLoadState metadata is no longer available) so that we can store it and use the URL as a key.
 *
 * See also: dataLoadMetaInfoForPlaylist
 */
fun OpdsFeed.withAbsoluteSelfUrl(urlLoaded: Url): OpdsFeed {
    return copy(links = links.withAbsoluteSelfLink(urlLoaded))
}


/**
 * Playlists that are being stored:
 *
 * 1) Url is as per the self link (which MUST be an absolute URL based on the school url).
 * 2) Etag is the metainfo.modified (which MUST NOT be null)
 * 3) last modified header is the stored time : see respect-datalayer-repository for notes on why
 *    this http header uses the stored time NOT the time of the actual modification.
 */
fun OpdsFeed.dataLoadMetaInfoForPlaylist() : DataLoadMetaInfo {
    val modified = metadata.modified ?: throw IllegalArgumentException()

    return DataLoadMetaInfo(
        url = requireSelfUrl(),
        lastModified = systemTimeInMillis(),
        etag = modified.toString(),
    )
}
