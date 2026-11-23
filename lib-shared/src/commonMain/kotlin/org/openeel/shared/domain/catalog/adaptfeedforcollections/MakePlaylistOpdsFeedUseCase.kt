package org.openeel.shared.domain.catalog.adaptfeedforcollections

import com.eygraber.uri.Uri
import io.ktor.http.Url
import org.openeel.lib.opds.model.OpdsFeed
import org.openeel.lib.opds.model.ReadiumLink
import org.openeel.libutil.ext.appendEndpointSegments
import kotlin.time.Clock

/**
 * Modify an OPDS feed to make it suitable for use as a school-based collection that is stored in
 * the xAPI Activity Profile API (see
 * [org.openeel.shared.domain.catalog.saveopdsfeed.SaveOpdsFeedUseCase])
 *
 * This will:
 * 1) Set a self link with an absolute url including a generated
 *    e.g. https://schoolname.example.org/playlist/00112233-4455-6677-8899-aabbccddeeff
 * 2) Set the last modified time.
 * 3) Add an owner link to identify the creator of the playlist.
 */
class MakePlaylistOpdsFeedUseCase(
    private val schoolUrl: Url,
) {

    operator fun invoke(
        base: OpdsFeed,
        feedUrl: Url,
        username: String,
    ): OpdsFeed {
        val ownerLink = ReadiumLink(
            href = getUserProfileUrl(schoolUrl, username),
            rel = listOf(REL_OWNER),
        )

        return base.copy(
            metadata = base.metadata.copy(
                identifier = Uri.parseOrNull(feedUrl.toString()),
                modified = Clock.System.now(),
            ),
            links = base.links + ownerLink,
        )
    }

    companion object {
        const val REL_OWNER = "https://respect.ustadmobile.com/ns/owner"
        private const val PATH_USER = "user"

        /**
         * Centralized way to build the user profile URL.
         * Uses appendEndpointSegments from UrlExt.kt to ensure correct slash handling.
         */
        fun getUserProfileUrl(schoolUrl: Url, username: String): String {
            return schoolUrl.appendEndpointSegments(PATH_USER, username).toString()
        }

    }
}