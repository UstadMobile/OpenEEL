package world.respect.shared.domain.bookmark

import io.ktor.http.Url
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.opds.model.LangMap
import world.respect.lib.opds.model.toStringMap
import world.respect.lib.xapi.OpenEelXapiConstants
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiActivityDefinition
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiContext
import world.respect.lib.xapi.model.XapiContextActivities
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiVerb

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

