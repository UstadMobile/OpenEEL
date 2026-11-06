package org.openeel.datalayer.db.host

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import org.openeel.datalayer.db.SchoolDirectoryDatabase
import org.openeel.datalayer.db.host.adapters.toEntity
import org.openeel.datalayer.db.host.adapters.toModel
import org.openeel.datalayer.db.host.entities.HostListEntity
import org.openeel.datalayer.host.HostListDataSource
import org.openeel.datalayer.host.HostListDataSourceLocal
import org.openeel.datalayer.respect.model.SchoolDirectory
import org.openeel.lib.dataloadstate.DataLoadMetaInfo
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.lib.dataloadstate.ext.isStillValid
import org.openeel.lib.dataloadstate.ext.requestEtagAndLastModified
import org.openeel.lib.dataloadstate.ext.responseETagAndLastModified

class HostListDataSourceDb(
    private val database: SchoolDirectoryDatabase,
    private val json: Json,
) : HostListDataSourceLocal {

    private fun HostListEntity?.toDataLoadState(
        url: Url,
        loadParams: DataLoadParams,
    ): DataLoadState<List<SchoolDirectory>> {
        val result = this?.toModel(json)
            ?: return NoDataLoadedState.notFound(metaInfo = DataLoadMetaInfo(url = url))
        return if (loadParams.requestHeaders.requestEtagAndLastModified().isStillValid(
                result.metaInfo.headers.responseETagAndLastModified()
            )
        ) {
            NoDataLoadedState.notModified(metaInfo = result.metaInfo)
        } else {
            result
        }
    }

    override fun listAsFlow(
        loadParams: DataLoadParams,
        params: HostListDataSource.GetListParams,
    ): Flow<DataLoadState<List<SchoolDirectory>>> =
        database.getHostListEntityDao().findByUrlAsFlow(params.url.toString()).map {
            it.toDataLoadState(params.url, loadParams)
        }


    override suspend fun updateLocal(
        url: Url,
        dataLoadResult: DataReadyState<List<SchoolDirectory>>,
    ) {
        val entity = dataLoadResult.toEntity(url, json)
        if (database.getHostListEntityDao().findByUrl(url.toString()) != entity) {
            database.getHostListEntityDao().upsert(entity)
        }
    }
}
