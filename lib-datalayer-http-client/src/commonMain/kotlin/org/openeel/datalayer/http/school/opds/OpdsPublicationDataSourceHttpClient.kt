package org.openeel.datalayer.http.school.opds

import io.ktor.client.HttpClient
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.datalayer.compatibleapps.model.RespectAppManifest
import org.openeel.datalayer.ext.getAsDataLoadState
import org.openeel.datalayer.ext.getDataLoadResultAsFlow
import org.openeel.lib.dataloadstate.ext.map
import org.openeel.datalayer.networkvalidation.BaseDataSourceValidationHelper
import org.openeel.datalayer.school.opds.OpdsPublicationDataSource
import org.openeel.datalayer.school.opds.ext.asOpdsPublication
import org.openeel.datalayer.school.opds.ext.withAbsoluteSelfUrl
import org.openeel.lib.opds.model.Publication

class OpdsPublicationDataSourceHttpClient(
    private val httpClient: HttpClient,
    private val publicationValidationHelper: BaseDataSourceValidationHelper? = null,
    private val json: Json,
) : OpdsPublicationDataSource {

    /**
     * RespectAppManifest (now deprecated in favor of using OpdsPublication)
     */
    private fun DataLoadState<JsonElement>.asPublicationIfRespectAppManifest(

    ): DataLoadState<Publication> {
        return this.map { element ->
            if(element is JsonObject && element.containsKey("defaultLaunchUri")) {
                json.decodeFromJsonElement(
                    RespectAppManifest.serializer(), element
                ).asOpdsPublication()
            }else {
                json.decodeFromJsonElement(Publication.serializer(), element)
            }
        }
    }

    override fun getByUrlAsFlow(
        url: Url,
        params: DataLoadParams,
        referrerUrl: Url?,
        expectedPublicationId: String?
    ): Flow<DataLoadState<Publication>> {
        return httpClient.getDataLoadResultAsFlow<JsonElement>(
            urlFn = { url },
            dataLoadParams = params,
            validationHelper = publicationValidationHelper,
        ).map { dataLoadResult ->
            dataLoadResult.asPublicationIfRespectAppManifest().map {
                it.withAbsoluteSelfUrl(url)
            }
        }
    }

    override suspend fun getByUrl(
        url: Url,
        params: DataLoadParams,
        referrerUrl: Url?,
        expectedPublicationId: String?
    ): DataLoadState<Publication> {
        return httpClient.getAsDataLoadState<JsonElement>(
            url = url,
            validationHelper = publicationValidationHelper,
        ).asPublicationIfRespectAppManifest().map {
            it.withAbsoluteSelfUrl(url)
        }
    }
}
