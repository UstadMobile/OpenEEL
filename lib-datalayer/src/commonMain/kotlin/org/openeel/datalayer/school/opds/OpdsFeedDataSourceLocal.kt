package world.respect.datalayer.school.opds

import io.ktor.http.Url
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.datalayer.networkvalidation.BaseDataSourceValidationHelper
import world.respect.lib.opds.model.OpdsFeed

interface OpdsFeedDataSourceLocal: OpdsFeedDataSource, BaseDataSourceValidationHelper {

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
