package world.respect.datalayer.school.opds

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.opds.model.OpdsFeed

/**
 * OpdsFeedDataSource: OpdsFeeds can be either:
 *  a) External OPDS feeds that come from app developers or any other third party source, which are
 *     hosted on external URLs. This is read-only information from the point of view of the RESPECT
 *     app and server.
 *  b) Playlists that are created by RESPECT app users within the RESPECT app.
 *
 * The need to handle third party external data from other servers is the key difference between
 * this DataSource and others. Other DataSources (e.g. Person, Assignment, etc) handle data where
 * there is only one upstream remote endpoint (the RESPECT school endpoint).
 *
 * There is a 1:1 relationship between the OpdsFeed URL and the OpdsFeedEntity when stored locally.
 * There is no general "list" function.
 *
 * Playlists that are created by users on the RESPECT app:
 *  a) Have a URL in the form of http://school.example.org/playlist/uid
 *  b) Have the OpdsFeedMetadata.modified set to the time the user actually modified or changed the
 *     playlist (e.g. when the user clicked Save). It is not the same time as when the data is stored
 *     on any given node (including the server). See respect-datalayer-repository offline-first
 *     README.md notes.
 *  c) The HTTP ETag will be set using the OpdsFeedMetadata.modified value.
 *  d) The HTTP Last-Modified header will be the time the playlist was actually stored into the
 *     database on the node (eg server) serving the response. This is essential to ensure that HTTP
 *     responses remain compliant with the HTTP spec, as per the respect-datalayer-repository
 *     offline-first README.md notes.
 *
 * Client side:
 *  The remote datasource will fetch from a server as specified by the URL, which may, or may not,
 *  be the school URL.
 *
 * Server side:
 * The server is expected to 'crawl' the school's available OpdsFeeds such that it can handle
 * search queries.
 *
 * OpdsFeeds Playlists are NOT modified or transformed by any server: they are copied. E.g. if a
 * feed from an app developer has poor metadata, the server will NOT transform the original feed. The
 * school admin or school teachers can copy the app's feed, and then modify it.
 */
interface OpdsFeedDataSource {

    /**
     * Load an OPDS Feed from a given URL : essentially the same as getByUid for other data types.
     */
    fun getByUrlAsFlow(
        url: Url,
        params: DataLoadParams
    ):  Flow<DataLoadState<OpdsFeed>>


    suspend fun getByUrl(
        url: Url,
        params: DataLoadParams
    ): DataLoadState<OpdsFeed>

    companion object {

        const val PLAYLIST_ENDPOINT_NAME = "playlist"

    }

}