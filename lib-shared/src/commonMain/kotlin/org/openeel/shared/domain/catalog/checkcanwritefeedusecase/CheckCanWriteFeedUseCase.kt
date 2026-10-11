package org.openeel.shared.domain.catalog.checkcanwritefeedusecase

import io.ktor.http.Url
import org.openeel.datalayer.db.school.GetAuthenticatedPersonUseCase
import org.openeel.datalayer.db.school.ext.isAdminOrTeacher
import org.openeel.lib.xapi.OpenEelXapiConstants
import org.openeel.libutil.ext.appendEndpointSegments

/**
 * Checks if the authenticated user can write a feed with a given URL. In order for the user to be
 * able to write a feed:
 *
 * a) The feedUrl MUST be from the Activity Profile Resource (e.g. the Url returned by xapiUrl()/activities/profile)
 * b) The feedUrl MUST use the profile Id [org.openeel.lib.xapi.OpenEelXapiConstants.ACTIVITY_PROFILEID_OPDS_COLLECTION]
 * c) The authenticated user MUST be a teacher or administrator.
 */
class CheckCanWriteFeedUseCase(
    private val getAuthenticatedPersonUseCase: GetAuthenticatedPersonUseCase,
    private val xapiUrl: suspend () -> Url,
) {

    suspend operator fun invoke(feedUrl: Url): Boolean {
        if(getAuthenticatedPersonUseCase()?.isAdminOrTeacher() != true)
            return false

        if (feedUrl.parameters["profileId"] != OpenEelXapiConstants.ACTIVITY_PROFILEID_OPDS_COLLECTION) {
            return false
        }

        val expectedProfileBaseUrl = xapiUrl().appendEndpointSegments("activities", "profile")
        if (feedUrl.protocol != expectedProfileBaseUrl.protocol ||
            !feedUrl.host.equals(expectedProfileBaseUrl.host, ignoreCase = true) ||
            feedUrl.port != expectedProfileBaseUrl.port ||
            feedUrl.rawSegments.filter { it.isNotEmpty() } != expectedProfileBaseUrl.rawSegments.filter { it.isNotEmpty() }
        ) {
            return false
        }

        return true
    }

}