package org.openeel.shared.domain.bookmark

import io.ktor.http.Url
import org.openeel.datalayer.SchoolDataSource
import org.openeel.lib.opds.model.LangMap
import org.openeel.lib.opds.model.toStringMap
import org.openeel.lib.xapi.OpenEelXapiConstants
import org.openeel.lib.xapi.model.XapiActivity
import org.openeel.lib.xapi.model.XapiActivityDefinition
import org.openeel.lib.xapi.model.XapiAgent
import org.openeel.lib.xapi.model.XapiContext
import org.openeel.lib.xapi.model.XapiContextActivities
import org.openeel.lib.xapi.model.XapiStatement
import org.openeel.lib.xapi.model.XapiVerb

/**
 * Add a bookmark for a learning unit (Opds Publication) or collection (OpdsFeed - not yet in use).
 *
 * Uses the bookmarklet xAPI recipe as per : https://registry.tincanapi.com/#profile/23
 *
 * As per the recipe the Activity ID of the bookmarked statement is the OPDS Url (publication or
 * feed).
 */
class AddBookmarkUseCase(
    private val schoolDataSource: SchoolDataSource,
) {

    /**
     * @param agent the user who is bookmarking something
     * @param url the Url to be bookmarked
     * @param title the title of the bookmarked item
     */
    suspend operator fun invoke(
        agent: XapiAgent,
        url: Url,
        title: LangMap? = null,
    ) {
        schoolDataSource.xapiResource.statements.post(
            listOf(
                XapiStatement(
                    actor = agent,
                    verb = XapiVerb(id = XapiVerb.ID_BOOKMARKED),
                    `object` = XapiActivity(
                        id = url.toString(),
                        definition = title?.let {
                            XapiActivityDefinition(
                                name = it.toStringMap(),
                            )
                        }
                    ),
                    context = XapiContext(
                        contextActivities = XapiContextActivities(
                            category = listOf(
                                XapiActivity(id = OpenEelXapiConstants.CATEGORY_BOOKMARK_RECIPE)
                            )
                        )
                    ),
                )
            )
        )
    }

}

