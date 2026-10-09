package org.openeel.shared.domain.catalog.saveopdsfeed

import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.http.toHttpDate
import io.ktor.util.sha1
import kotlinx.serialization.json.Json
import org.openeel.datalayer.school.opds.OpdsFeedDataSourceLocal
import org.openeel.datalayer.school.opds.ext.requireSelfUrl
import org.openeel.lib.dataloadstate.DataLoadMetaInfo
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.datetime.toGMTDate
import org.openeel.lib.opds.model.OpdsFeed
import org.openeel.lib.xapi.OpenEelXapiConstants
import org.openeel.lib.xapi.ext.encodeToXapiDocument
import org.openeel.lib.xapi.resources.XapiActivityProfileResource
import kotlin.time.Clock

/**
 * Saving an OpdsFeed is a little bit different because it involves handling multiple data sources:
 *  1) The xAPI activity profile resource which is used to actually store the data and make it
 *     available on other devices.
 *  2) The OpdsFeedDataSource which needs to be updated so that the OpdsFeed appears as expected
 *     when browsing locally.
 */
class SaveOpdsFeedUseCase(
    private val xapiActivityProfileResource: XapiActivityProfileResource,
    private val opdsFeedDataSourceLocal: OpdsFeedDataSourceLocal,
    private val json: Json,
) {

    suspend operator fun invoke(feed: OpdsFeed) {
        val feedUrl = feed.requireSelfUrl()

        val updated = Clock.System.now().toGMTDate()
        val xapiDocument = json.encodeToXapiDocument(
            serializer = OpdsFeed.serializer(), value = feed, updated = updated
        )

        xapiActivityProfileResource.put(
            params = XapiActivityProfileResource.SingleDocumentParams(
                profileId = OpenEelXapiConstants.ACTIVITY_PROFILEID_OPDS_COLLECTION,
                activityId = feedUrl.parameters["activityId"] ?: throw IllegalArgumentException()
            ),
            document = xapiDocument,
        )

        opdsFeedDataSourceLocal.updateLocal(
            url = feedUrl,
            dataLoadResult = DataReadyState(
                data = feed,
                metaInfo = DataLoadMetaInfo(
                    headers = headersOf(
                        HttpHeaders.LastModified to listOf(updated.toHttpDate()),
                        HttpHeaders.ETag to listOf(sha1(xapiDocument.contentsAsByteArray()).toHexString()),
                    )
                )
            )
        )
    }

}