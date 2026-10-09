package world.respect.datalayer.school

import io.ktor.util.StringValues
import kotlinx.coroutines.flow.Flow
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.datalayer.school.model.SchoolPermissionGrant
import world.respect.datalayer.shared.WritableDataSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.params.GetListCommonParams

interface SchoolPermissionGrantDataSource: WritableDataSource<SchoolPermissionGrant> {

    data class GetListParams(
        val common: GetListCommonParams = GetListCommonParams(),
    ) {
        companion object {
            fun fromParams(params: StringValues) : GetListParams {
                return GetListParams(
                    common = GetListCommonParams.fromParams(params)
                )
            }
        }
    }

    fun findByGuidAsFlow(guid: String): Flow<DataLoadState<SchoolPermissionGrant>>

    suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<SchoolPermissionGrant>

    fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: GetListParams,
    ): IPagingSourceFactory<Int, SchoolPermissionGrant>

    suspend fun list(
        loadParams: DataLoadParams,
        params: GetListParams
    ): DataLoadState<List<SchoolPermissionGrant>>

    override suspend fun store(
        list: List<SchoolPermissionGrant>,
    )


    companion object {

        const val ENDPOINT_NAME = "schoolpermissiongrant"

    }

}
