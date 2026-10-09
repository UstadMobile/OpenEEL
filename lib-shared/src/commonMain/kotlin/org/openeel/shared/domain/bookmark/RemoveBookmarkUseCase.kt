package world.respect.shared.domain.bookmark
import io.ktor.http.Url
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.OpenEelXapiConstants
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiContext
import world.respect.lib.xapi.model.XapiContextActivities
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiStatementRef
import world.respect.lib.xapi.model.XapiVerb
import world.respect.lib.xapi.resources.XapiStatementsResource
/**
 * UseCase to remove a bookmark by voiding the bookmark xAPI statement(s) for a given activity.
 */
class RemoveBookmarkUseCase(
    private val schoolDataSource: SchoolDataSource,
) {
    suspend operator fun invoke(
        statements: List<XapiStatement>,
    ) {
        schoolDataSource.xapiResource.statements.post(
            statements.map { stmt ->
                val stmtId = stmt.id
                    ?: throw IllegalStateException("Cannot void bookmark: statement has no id")
                XapiStatement(
                    actor = stmt.actor,
                    verb = XapiVerb(id = XapiVerb.ID_VOIDED),
                    `object` = XapiStatementRef(id = stmtId.toString()),
                    context = XapiContext(
                        contextActivities = XapiContextActivities(
                            category = listOf(
                                XapiActivity(id = OpenEelXapiConstants.CATEGORY_BOOKMARK_RECIPE)
                            )
                        )
                    ),
                )
            }
        )
    }

    suspend operator fun invoke(
        agent: XapiAgent,
        url: Url,
    ) {
        val existingStatements = schoolDataSource.xapiResource.statements.get(
            listParams = XapiStatementsResource.GetStatementParams(
                agent = agent,
                verb = XapiVerb.ID_BOOKMARKED,
                activity = url.toString(),
                relatedActivities = true,
            )
        ).dataOrNull()?.statements
            ?: throw IllegalStateException("Cannot remove bookmark: failed to retrieve statements for activity $url")

        if (existingStatements.isEmpty()) {
            throw IllegalStateException("Cannot remove bookmark: no bookmark statement found for activity $url")
        }

        invoke(existingStatements)
    }
}
