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
import org.openeel.lib.opds.model.LangMapStringValue
import org.openeel.lib.opds.model.toStringMap
import org.openeel.lib.xapi.model.XapiActor
import org.openeel.lib.xapi.resources.XapiStatementsResource
import org.openeel.shared.domain.xapi.createPinCollectionStatement
import kotlin.time.Clock

/**
 * Saving an OpdsFeed is a little bit different because it involves handling multiple data sources:
 *  1) The xAPI activity profile resource which is used to actually store the data and make it
 *     available on other devices.
 *  2) The OpdsFeedDataSource which needs to be updated so that the OpdsFeed appears as expected
 *     when browsing locally.
 *  3) An xAPI statement as per the Collections Listing recipe (README_COLLECTIONS_LISTING_RECIPE.md)
 *     is posted so that the collection is "pinned" and shows up in a listing of available
 *     collections.
 */
class SaveOpdsFeedUseCase(
    private val xapiActivityProfileResource: XapiActivityProfileResource,
    private val xapiStatementsResource: XapiStatementsResource,
    private val opdsFeedDataSourceLocal: OpdsFeedDataSourceLocal,
    private val json: Json,
) {

    suspend operator fun invoke(feed: OpdsFeed, actor: XapiActor) {
        val feedUrl = feed.requireSelfUrl()
        val activityId = feedUrl.parameters["activityId"] ?: throw IllegalArgumentException()

        val now = Clock.System.now()
        val updated = now.toGMTDate()

        //As per OpdsFeedDataSource.kt: playlists must have metadata.modified set to the time the
        //user actually clicked save, otherwise OpdsFeedDataSourceDb.updateLocal will skip storing
        //the update because it is not newer than what is already stored locally.
        val feedToSave = feed.copy(metadata = feed.metadata.copy(modified = now))

        val xapiDocument = json.encodeToXapiDocument(
            serializer = OpdsFeed.serializer(), value = feedToSave, updated = updated
        )

        xapiActivityProfileResource.put(
            params = XapiActivityProfileResource.SingleDocumentParams(
                profileId = OpenEelXapiConstants.ACTIVITY_PROFILEID_OPDS_COLLECTION,
                activityId = activityId,
            ),
            document = xapiDocument,
        )

        opdsFeedDataSourceLocal.updateLocal(
            url = feedUrl,
            dataLoadResult = DataReadyState(
                data = feedToSave,
                metaInfo = DataLoadMetaInfo(
                    headers = headersOf(
                        HttpHeaders.LastModified to listOf(updated.toHttpDate()),
                        HttpHeaders.ETag to listOf(sha1(xapiDocument.contentsAsByteArray()).toHexString()),
                    )
                )
            )
        )

        xapiStatementsResource.post(
            listOf(
                createPinCollectionStatement(
                    collectionActivityId = activityId,
                    collectionName = LangMapStringValue(feedToSave.metadata.title).toStringMap(),
                    collectionDescription = feedToSave.metadata.description?.let {
                        LangMapStringValue(it).toStringMap()
                    },
                    opdsCollectionLink = feedUrl.toString(),
                    actor = actor,
                )
            )
        )
    }
}