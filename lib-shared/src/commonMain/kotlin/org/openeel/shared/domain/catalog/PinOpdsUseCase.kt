package org.openeel.shared.domain.catalog

import kotlinx.serialization.json.JsonPrimitive
import org.openeel.datalayer.school.opds.ext.requireSelfUrl
import org.openeel.lib.opds.model.LangMapStringValue
import org.openeel.lib.opds.model.OpdsFeed
import org.openeel.lib.opds.model.toStringMap
import org.openeel.lib.xapi.OpenEelXapiConstants
import org.openeel.lib.xapi.auth.GetXapiAuthorityActorUseCase
import org.openeel.lib.xapi.model.XapiActivity
import org.openeel.lib.xapi.model.XapiActivityDefinition
import org.openeel.lib.xapi.model.XapiContext
import org.openeel.lib.xapi.model.XapiContextActivities
import org.openeel.lib.xapi.model.XapiObjectType
import org.openeel.lib.xapi.model.XapiStatement
import org.openeel.lib.xapi.model.XapiVerb
import org.openeel.lib.xapi.resources.XapiStatementsResource

/**
 * Pin an OpdsFeed. This is very similar to bookmarking.
 */
class PinOpdsUseCase(
    private val statementsResource: XapiStatementsResource,
    private val getXapiAuthorityActorUseCase: GetXapiAuthorityActorUseCase,
) {

    suspend operator fun invoke(
        opdsFeed: OpdsFeed,
        contextCategoryId: String,
    ) {
        val feedUrl = opdsFeed.requireSelfUrl()

        statementsResource.post(
            listOf(
                XapiStatement(
                    actor = getXapiAuthorityActorUseCase() ?: throw IllegalStateException(),
                    verb = XapiVerb(id = XapiVerb.ID_PINNED),
                    `object` = XapiActivity(
                        objectType = XapiObjectType.Activity,
                        id = feedUrl.toString(),
                        definition = XapiActivityDefinition(
                            name = LangMapStringValue(opdsFeed.metadata.title).toStringMap(),
                            description = opdsFeed.metadata.description?.let {
                                LangMapStringValue(it).toStringMap()
                            },
                            extensions = mapOf(
                                OpenEelXapiConstants.ACTIVITY_EXTENSION_WEBPUB_MANIFEST_LINK to
                                        JsonPrimitive(feedUrl.toString())
                            ),
                        ),
                    ),
                    context = XapiContext(
                        contextActivities = XapiContextActivities(
                            category = listOf(
                                XapiActivity(id = contextCategoryId)
                            ),
                        ),
                    ),
                )
            )
        )
    }

    suspend operator fun invoke(
        statement: XapiStatement
    ) {

    }

}