package org.openeel.datalayer.school.opds

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import org.openeel.datalayer.networkvalidation.BaseDataSourceValidationHelper
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.opds.model.OpdsFeed
import org.openeel.lib.opds.model.ext.OpdsFeedSearchMatch

interface OpdsFeedDataSourceLocal: OpdsFeedDataSource, BaseDataSourceValidationHelper {

    data class GetListParams(
        val title: String = "",
    )

    /**
     * Search cached feed items by title, returning their original feed/group indexes.
     */
    fun searchByTitleAsFlow(
        url: Url,
        listParams: GetListParams,
    ): Flow<List<OpdsFeedSearchMatch>>

    /**
     * The update local is a little different for OpdsFeed because the data can come from different
     * servers. External servers may set the etag and last-modified any way they wish, so we need
     * the DataReadyState to access metadata.
     */
    suspend fun updateLocal(
        url: Url,
        dataLoadResult: DataReadyState<OpdsFeed>,
    )

}
