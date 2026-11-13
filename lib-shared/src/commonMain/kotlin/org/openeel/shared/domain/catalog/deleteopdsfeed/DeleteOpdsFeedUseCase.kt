package org.openeel.shared.domain.catalog.deleteopdsfeed

import io.ktor.http.Url
import org.openeel.datalayer.school.opds.OpdsFeedDataSourceLocal
import org.openeel.lib.xapi.OpenEelXapiConstants
import org.openeel.lib.xapi.resources.XapiActivityProfileResource
import org.openeel.shared.domain.catalog.unpinopdsfeedusecase.UnpinOpdsFeedUseCase

class DeleteOpdsFeedUseCase(
    private val xapiActivityProfileResource: XapiActivityProfileResource,
    private val opdsFeedDataSourceLocal: OpdsFeedDataSourceLocal,
    private val unpinOpdsFeedUseCase: UnpinOpdsFeedUseCase,
) {

    suspend operator fun invoke(
        feedUrl: Url
    ) {
        val activityId = feedUrl.parameters["activityId"] ?: throw IllegalArgumentException()

        xapiActivityProfileResource.delete(
            params = XapiActivityProfileResource.SingleDocumentParams(
                profileId = OpenEelXapiConstants.ACTIVITY_PROFILEID_OPDS_COLLECTION,
                activityId = activityId,
            )
        )

        opdsFeedDataSourceLocal.delete(feedUrl)

        unpinOpdsFeedUseCase(feedUrl.toString())
    }

}