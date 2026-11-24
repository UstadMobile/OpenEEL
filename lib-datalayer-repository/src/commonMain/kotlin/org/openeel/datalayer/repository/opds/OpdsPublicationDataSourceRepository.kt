package org.openeel.datalayer.repository.opds

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.ext.combineWithRemote
import org.openeel.datalayer.school.opds.OpdsPublicationDataSource
import org.openeel.datalayer.school.opds.OpdsPublicationDataSourceLocal
import org.openeel.lib.dataloadstate.ext.copyLoadState
import org.openeel.lib.opds.model.Publication

/**
 * Note: referrerUrl and expectedPublicationId are used ONLY with the local data source
 * - see [OpdsPublicationDataSource.getByUrl].
 */
class OpdsPublicationDataSourceRepository(
    private val local: OpdsPublicationDataSourceLocal,
    private val remote: OpdsPublicationDataSource,
): OpdsPublicationDataSource {

    /**
     * When referrerUrl and expectedPublicationId are used to load a publication "preview" this
     * needs to be detectable by the ViewModel etc.
     *
     * The [org.openeel.lib.dataloadstate.DataLoadMetaInfo.url] will be null if data is loaded
     * as a "preview". Normally the combineWithRemote function would replace any Url provided by
     * the local datasource with the url from the remote state.
     */
    private fun <T: Any> DataLoadState<T>.copyWithLocalUrl(): DataLoadState<T> {
        return copyLoadState(
            metaInfo = metaInfo.copy(
                url = localState?.metaInfo?.url
            )
        )
    }

    override fun getByUrlAsFlow(
        url: Url,
        params: DataLoadParams,
        referrerUrl: Url?,
        expectedPublicationId: String?
    ): Flow<DataLoadState<Publication>> {
        return local.getByUrlAsFlow(
            url = url,
            params = params,
            referrerUrl = referrerUrl,
            expectedPublicationId = expectedPublicationId,
        ).combineWithRemote(
            remoteFlow = remote.getByUrlAsFlow(
                url = url,
                params = params,
                referrerUrl = null,
                expectedPublicationId = null,
            ).onEach { remoteData ->
                if(remoteData is DataReadyState) {
                    local.updateOpdsPublication(remoteData)
                }
            }
        ).map {
            it.copyWithLocalUrl()
        }
    }

    override suspend fun getByUrl(
        url: Url,
        params: DataLoadParams,
        referrerUrl: Url?,
        expectedPublicationId: String?
    ): DataLoadState<Publication> {
        val remoteData = remote.getByUrl(url, params, null, null)
        if(remoteData is DataReadyState)
            local.updateOpdsPublication(remoteData)

        return local.getByUrl(
            url, params, referrerUrl, expectedPublicationId
        ).combineWithRemote(remoteData).copyWithLocalUrl()
    }

}
