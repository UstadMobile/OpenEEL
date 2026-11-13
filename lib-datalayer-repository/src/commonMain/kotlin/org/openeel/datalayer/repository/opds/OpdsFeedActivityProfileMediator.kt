package org.openeel.datalayer.repository.opds

import io.ktor.http.Url
import io.ktor.http.protocolWithAuthority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
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
import org.openeel.libutil.ext.appendEndpointSegments

/**
 * The [OpdsFeed] data source handling is a little different because whilst normally an OpdsFeed
 * loads from an external HttpServer (read-only) through a normal HttpClient/local caching database
 * setup, an OpdsFeed can also be edited and saved using the [XapiActivityProfileResource]
 * as per README_OPDS_COLLECTIONS_ACTIVITY_PROFILE.md .
 *
 * When loading an [OpdsFeed] that is stored using the [XapiActivityProfileResource] (as determined
 * by checking the URL) then loading is done through the [XapiActivityProfileResource] and the
 * [OpdsFeedDataSourceLocal] is updated accordingly (such that search etc will work as expected).
 *
 * @param opdsLocal the [OpdsFeedDataSourceLocal]
 * @param opdsRepo the [OpdsFeedDataSourceRepository] that handles normal offline-first [OpdsFeed] loading
 * @param activityProfileRepo the [XapiActivityProfileResource] being used to save/load OpdsFeeds
 * @param json Kotlinx serialization Json
 * @param xapiUrl the function that provides the xAPI endpoint base Url
 */
class OpdsFeedActivityProfileMediator(
    private val opdsLocal: OpdsFeedDataSourceLocal,
    private val opdsRepo: OpdsFeedDataSource,
    private val activityProfileRepo: XapiActivityProfileResource,
    private val json: Json,
    private val xapiUrl: suspend () -> Url,
) : OpdsFeedDataSource{

    private suspend fun Url.isFromXapiActivityProfileResource(): Boolean {
        val xapiUrlVal = xapiUrl()
        return xapiUrlVal.protocolWithAuthority == this.protocolWithAuthority &&
                this.encodedPath == xapiUrlVal.appendEndpointSegments("activities/profile").encodedPath
    }

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
        return flow {
            emitAll(
                flow = if(url.isFromXapiActivityProfileResource()) {
                    activityProfileRepo.getAsFlow(
                        params = url.toSingleDocParams(),
                        dataLoadParams = params,
                    ).map { dataLoadState ->
                        dataLoadState.toOpdsFeed().also {
                            updateOpdsLocalIfNeeded(url, it)
                        }
                    }
                }else {
                    opdsRepo.getByUrlAsFlow(url, params)
                }
            )
        }
    }

    override suspend fun getByUrl(
        url: Url,
        params: DataLoadParams
    ): DataLoadState<OpdsFeed> {
        return if(url.isFromXapiActivityProfileResource()) {
            activityProfileRepo.get(
                params = url.toSingleDocParams(),
                dataLoadParams = params,
            ).toOpdsFeed().also { dataLoadState ->
                updateOpdsLocalIfNeeded(url, dataLoadState)
            }
        }else {
            opdsRepo.getByUrl(url, params)
        }
    }
}