package world.respect.datalayer.school.opds

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.opds.model.Publication

interface OpdsPublicationDataSource {

    /**
     *
     * @param url
     * @param params
     * @param referrerUrl where a publication is being loaded based on following a link from an
     *        opds feed, providing the URL and publicationId of the feed can be used to load a first
     *        version from the cache
     * @param expectedPublicationId where a publication is being loaded based on following a link from an
     *        opds feed, providing the URL and publicationId of the feed can be used to load a first
     *        version from the cache
     */
    fun getByUrlAsFlow(
        url: Url,
        params: DataLoadParams,
        referrerUrl: Url?,
        expectedPublicationId: String?,
    ): Flow<DataLoadState<Publication>>


    /**
     *
     * @param url
     * @param params
     * @param referrerUrl where a publication is being loaded based on following a link from an
     *        opds feed, providing the URL and publicationId of the feed can be used to load a first
     *        version from the cache
     * @param expectedPublicationId where a publication is being loaded based on following a link from an
     *        opds feed, providing the URL and publicationId of the feed can be used to load a first
     *        version from the cache
     */
    suspend fun getByUrl(
        url: Url,
        params: DataLoadParams,
        referrerUrl: Url? = null,
        expectedPublicationId: String? = null,
    ): DataLoadState<Publication>


}