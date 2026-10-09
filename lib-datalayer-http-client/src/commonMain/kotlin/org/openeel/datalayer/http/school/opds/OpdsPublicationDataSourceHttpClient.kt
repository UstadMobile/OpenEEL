package world.respect.datalayer.http.school.opds

import io.ktor.client.HttpClient
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.datalayer.compatibleapps.model.RespectAppManifest
import world.respect.datalayer.ext.getAsDataLoadState
import world.respect.datalayer.ext.getDataLoadResultAsFlow
import world.respect.lib.dataloadstate.ext.map
import world.respect.datalayer.networkvalidation.BaseDataSourceValidationHelper
import world.respect.datalayer.school.opds.OpdsPublicationDataSource
import world.respect.datalayer.school.opds.ext.asOpdsPublication
import world.respect.datalayer.school.opds.ext.withAbsoluteSelfUrl
import world.respect.lib.opds.model.Publication

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
