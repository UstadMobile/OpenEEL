package world.respect.shared.domain.sharelink

import io.ktor.http.URLBuilder
import io.ktor.http.Url
import world.respect.datalayer.school.opds.OpdsFeedDataSource
import world.respect.libutil.ext.RESPECT_SCHOOL_LINK_DIVIDER
import world.respect.libutil.ext.RESPECT_SCHOOL_LINK_SEGMENT
import world.respect.libutil.ext.appendEndpointPathSegments
import world.respect.libutil.ext.schoolUrlOrNull

/**
 * A playlist share link as per the school link convention documented on Url.schoolUrlOrNull.
 */
data class PlaylistShareLink(
    val schoolUrl: Url,
    val playlistUuid: String,
)

/**
 * Creates (and parses) playlist share links. As per the school link convention documented on
 * Url.schoolUrlOrNull, a share link is in the form:
 *
 * {schoolUrl}/respect_school_link/playlist/{playlistUuid}
 */
class CreatePlaylistShareLinkUseCase(
    private val schoolUrl: Url,
) {
    operator fun invoke(playlistUrl: Url): Url {
        val playlistUuid = playlistUrl.pathSegments.lastOrNull { it.isNotBlank() }
            ?: throw IllegalArgumentException(
                "Cannot extract playlist UUID from URL: $playlistUrl"
            )

        return URLBuilder(schoolUrl).apply {
            appendEndpointPathSegments(
                listOf(
                    RESPECT_SCHOOL_LINK_SEGMENT,
                    OpdsFeedDataSource.PLAYLIST_ENDPOINT_NAME,
                    playlistUuid,
                )
            )
        }.build()
    }

    companion object {

        /**
         * The linkdetail (the part after the school link divider) of a playlist share link is
         * exactly two segments: the playlist endpoint name and the playlist uuid.
         */
        private const val LINK_DETAIL_SEGMENTS = 2

        /**
         * Parse a share link created by invoke. The school url
         * is resolved using schoolUrlOrNull.
         *
         * @return the school url and playlist uuid, or null if this is not a playlist share link.
         */
        fun parseOrNull(url: Url): PlaylistShareLink? {
            val schoolUrl = url.schoolUrlOrNull() ?: return null

            val linkDetail = url.toString()
                .substringAfter(RESPECT_SCHOOL_LINK_DIVIDER)
                .substringBefore('?')
                .substringBefore('#')
                .split('/')
                .filter { it.isNotBlank() }

            val (endpointName, playlistUuid) = linkDetail
                .takeIf { it.size == LINK_DETAIL_SEGMENTS } ?: return null

            if (endpointName != OpdsFeedDataSource.PLAYLIST_ENDPOINT_NAME) return null

            return PlaylistShareLink(
                schoolUrl = schoolUrl,
                playlistUuid = playlistUuid,
            )
        }
    }
}