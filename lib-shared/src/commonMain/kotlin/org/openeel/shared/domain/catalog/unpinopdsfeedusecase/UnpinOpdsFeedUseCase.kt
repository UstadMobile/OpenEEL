package org.openeel.shared.domain.catalog.unpinopdsfeedusecase

import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.xapi.OpenEelXapiConstants
import org.openeel.lib.xapi.auth.GetXapiAuthorityActorUseCase
import org.openeel.lib.xapi.ext.requireId
import org.openeel.lib.xapi.model.XapiActivity
import org.openeel.lib.xapi.model.XapiContext
import org.openeel.lib.xapi.model.XapiContextActivities
import org.openeel.lib.xapi.model.XapiStatement
import org.openeel.lib.xapi.model.XapiStatementRef
import org.openeel.lib.xapi.model.XapiVerb
import org.openeel.lib.xapi.resources.XapiResource
import org.openeel.lib.xapi.resources.XapiStatementsResource

/**
 * Unpin a collection that was pinned using the PinOpdsFeedUseCase. Similar to bookmarks.
 */
class UnpinOpdsFeedUseCase(
    private val getXapiAuthorityActorUseCase: GetXapiAuthorityActorUseCase,
    private val xapiResource: XapiResource,
) {

    suspend operator fun invoke(
        activityId: String
    ) {
        invoke(
            pinStatements = xapiResource.statements.get(
                listParams = XapiStatementsResource.GetStatementParams(
                    verb = XapiVerb.ID_PINNED,
                    activity = activityId,
                )
            ).dataOrNull()?.statements ?: throw IllegalStateException("Could not load statements")
        )
    }

    suspend operator fun invoke(
        pinStatements: List<XapiStatement>
    ) {
        val actor = getXapiAuthorityActorUseCase() ?: throw IllegalStateException("No actor")
        xapiResource.takeIf {
            pinStatements.isNotEmpty()
        }?.statements?.post(
            pinStatements.map { pinStmt ->
                XapiStatement(
                    actor = actor,
                    verb = XapiVerb(id = XapiVerb.ID_VOIDED),
                    `object` = XapiStatementRef(id = pinStmt.requireId().toString()),
                    context = XapiContext(
                        contextActivities = XapiContextActivities(
                            category = listOf(
                                XapiActivity(
                                    id = OpenEelXapiConstants.CATEGORY_COLLECTION_LISTING_RECIPE,
                                )
                            )
                        )
                    ),
                )
            }
        )
    }

}