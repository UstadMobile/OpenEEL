package world.respect.datalayer.school.domain

import com.eygraber.uri.Uri
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import world.respect.datalayer.school.opds.ext.withAbsoluteSelfUrl
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.opds.model.ReadiumLink
import world.respect.lib.xapi.OpenEelXapiConstants
import world.respect.libutil.ext.appendEndpointPathSegments
import world.respect.libutil.ext.appendEndpointSegments
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Modify an OPDS feed to make it suitable for use as a school-based playlist. This will:
 * 1) Change the URL to the schoolurl/playlist/uuid
 *    e.g. https://schoolname.example.org/playlist/00112233-4455-6677-8899-aabbccddeeff
 * 2) Set the last modified time.
 * 3) Add an owner link to identify the creator of the playlist.
 */
class MakePlaylistOpdsFeedUseCase(
    private val schoolUrl: Url,
    private val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource,
) {

    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(
        base: OpdsFeed,
        username: String,
        uuid: Uuid = Uuid.random(),
    ): OpdsFeed {
        val xapiUrl = schoolDirectoryEntryDataSource.getSchoolDirectoryEntryByUrl(
            schoolUrl
        ).dataOrNull()?.xapi ?: throw IllegalStateException("No Xapi URL for $schoolUrl")

        val feedUrl = URLBuilder(xapiUrl).apply {
            appendEndpointPathSegments(listOf("activities", "profile"))
            encodedParameters["activityId"] = UrlEncoderUtil.encode(
                schoolUrl.appendEndpointSegments("collections", Uuid.random().toString()).toString()
            )
            encodedParameters["profileId"] = UrlEncoderUtil.encode(
                OpenEelXapiConstants.ACTIVITY_PROFILEID_OPDS_COLLECTION
            )
        }.build()

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
        ).withAbsoluteSelfUrl(feedUrl)
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