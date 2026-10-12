package org.openeel.datalayer.repository.opds

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.ext.combineWithRemote
import org.openeel.datalayer.school.opds.OpdsPublicationDataSource
import org.openeel.datalayer.school.opds.OpdsPublicationDataSourceLocal
import org.openeel.lib.opds.model.Publication

class OpdsPublicationDataSourceRepository(
    private val local: OpdsPublicationDataSourceLocal,
    private val remote: OpdsPublicationDataSource,
): OpdsPublicationDataSource {

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
                referrerUrl = referrerUrl,
                expectedPublicationId = expectedPublicationId,
            ).onEach { remoteData ->
                if(remoteData is DataReadyState) {
                    local.updateOpdsPublication(remoteData)
                }
            }
        )
    }

    override suspend fun getByUrl(
        url: Url,
        params: DataLoadParams,
        referrerUrl: Url?,
        expectedPublicationId: String?
    ): DataLoadState<Publication> {
        val remoteData = remote.getByUrl(url, params, referrerUrl, expectedPublicationId)
        if(remoteData is DataReadyState)
            local.updateOpdsPublication(remoteData)

        return local.getByUrl(url, params, referrerUrl, expectedPublicationId)
    }

}
