package org.openeel.datalayer.repository.opds

import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.openeel.datalayer.school.opds.OpdsFeedDataSource
import org.openeel.datalayer.school.opds.OpdsFeedDataSourceLocal
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.lib.dataloadstate.ext.mapAsync
import org.openeel.lib.opds.model.OpdsFeed
import org.openeel.lib.xapi.ext.decodeFromXapiDocument
import org.openeel.lib.xapi.model.XapiDocument
import org.openeel.lib.xapi.resources.XapiActivityProfileResource

/**
 * The [OpdsFeed] data source handling is a little different because whilst normally an OpdsFeed
 * loads from an external HttpServer (read-only) through a normal HttpClient/local caching database
 * setup, an OpdsFeed can also be edited and saved using the [XapiActivityProfileResource]
 * as per README_OPDS_COLLECTIONS_ACTIVITY_PROFILE.md .
 *
 * When loading an [OpdsFeed] that is stored using the [XapiActivityProfileResource] (as determined
 * by checking the URL) then loading is done through the [XapiActivityProfileResource] and the
 * [OpdsFeedDataSourceLocal] is updated accordingly (such that search etc will work as expected).
 */
class OpdsFeedActivityProfileMediator(
    val opdsLocal: OpdsFeedDataSourceLocal,
    val opdsRepo: OpdsFeedDataSource,
    val activityProfileRepo: XapiActivityProfileResource,
    private val json: Json,
    private val xapiUrl: suspend () -> Url,
) : OpdsFeedDataSource{

    private suspend fun DataLoadState<XapiDocument>.toOpdsFeed(): DataLoadState<OpdsFeed> {
        return mapAsync { document ->
            withContext(Dispatchers.IO) {
                json.decodeFromXapiDocument(OpdsFeed.serializer(), document)
            }
        }
    }

    private fun Url.toSingleDocParams() : XapiActivityProfileResource.SingleDocumentParams {
        return XapiActivityProfileResource.SingleDocumentParams.fromParameters(parameters)
    }

    private suspend fun updateOpdsLocalIfNeeded(
        url: Url,
        dataLoadState: DataLoadState<OpdsFeed>
    ) {
        when{
            dataLoadState is DataReadyState -> {
                opdsLocal.updateLocal(url, dataLoadState)
            }

            (dataLoadState as? NoDataLoadedState)?.reason == NoDataLoadedState.Reason.NOT_FOUND -> {
                opdsLocal.delete(url)
            }
        }
    }

    override fun getByUrlAsFlow(
        url: Url,
        params: DataLoadParams
    ): Flow<DataLoadState<OpdsFeed>> {
        return if(url.toString().contains("activities/profile")) {
            activityProfileRepo.getAsFlow(
                params = url.toSingleDocParams()
            ).map { dataLoadState ->
                dataLoadState.toOpdsFeed().also {
                    updateOpdsLocalIfNeeded(url, it)
                }
            }
        }else {
            opdsRepo.getByUrlAsFlow(url, params)
        }
    }

    override suspend fun getByUrl(
        url: Url,
        params: DataLoadParams
    ): DataLoadState<OpdsFeed> {
        return if(url.toString().contains("activities/profile")) {
            activityProfileRepo.get(
                params = url.toSingleDocParams(),
            ).toOpdsFeed().also { dataLoadState ->
                updateOpdsLocalIfNeeded(url, dataLoadState)
            }
        }else {
            opdsRepo.getByUrl(url, params)
        }
    }
}