package org.openeel.datalayer.school

import io.ktor.util.StringValues
import kotlinx.coroutines.flow.Flow
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.datalayer.school.model.Clazz
import org.openeel.datalayer.shared.WritableDataSource
import org.openeel.datalayer.shared.paging.IPagingSourceFactory
import org.openeel.datalayer.shared.params.GetListCommonParams

interface ClassDataSource: WritableDataSource<Clazz> {

    data class GetListParams(
        val common: GetListCommonParams = GetListCommonParams(),
        val inviteGuid: String? = null,
    ) {
        companion object {

            fun fromParams(params: StringValues) : GetListParams {
                return GetListParams(
                    common = GetListCommonParams.fromParams(params),
                    inviteGuid = params[PARAM_NAME_INVITE_CODE],
                )
            }

        }
    }

    fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Clazz>>

    suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<Clazz>

    fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: GetListParams,
    ): IPagingSourceFactory<Int, Clazz>

    suspend fun list(
        loadParams: DataLoadParams,
        params: GetListParams
    ): DataLoadState<List<Clazz>>

    override suspend fun store(
        list: List<Clazz>,
    )


    companion object {

        const val ENDPOINT_NAME = "class"

        const val PARAM_NAME_INVITE_CODE = "inviteCode"

    }
}