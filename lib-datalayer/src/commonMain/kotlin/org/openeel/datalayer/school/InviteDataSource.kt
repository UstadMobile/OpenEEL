package org.openeel.datalayer.school

import io.ktor.util.StringValues
import kotlinx.coroutines.flow.Flow
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.datalayer.school.model.Invite2
import org.openeel.datalayer.shared.WritableDataSource
import org.openeel.datalayer.shared.paging.IPagingSourceFactory
import org.openeel.datalayer.shared.params.GetListCommonParams

interface InviteDataSource : WritableDataSource<Invite2> {

    data class GetListParams(
        val common: GetListCommonParams = GetListCommonParams(),
        val inviteCode:String? = null,
    ) {
        companion object {
            fun fromParams(stringValues: StringValues): GetListParams {
                return GetListParams(
                    common = GetListCommonParams.fromParams(stringValues),
                    inviteCode = stringValues[PARAM_NAME_INVITE_CODE],
                )
            }
        }
    }

    fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: GetListParams,
    ): IPagingSourceFactory<Int, Invite2>


    fun findByUidAsFlow(
        uid: String,
        loadParams: DataLoadParams,
    ): Flow<DataLoadState<Invite2>>

    suspend fun findByGuid(guid: String): DataLoadState<Invite2>

    suspend fun findByCode(code: String): DataLoadState<Invite2>

    override suspend fun store(list: List<Invite2>)

    companion object {
        const val ENDPOINT_NAME = "invite"
        const val PARAM_NAME_INVITE_CODE = "inviteCode"

    }
}
