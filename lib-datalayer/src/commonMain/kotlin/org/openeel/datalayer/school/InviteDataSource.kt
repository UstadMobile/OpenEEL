package world.respect.datalayer.school

import io.ktor.util.StringValues
import kotlinx.coroutines.flow.Flow
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.datalayer.school.model.Invite2
import world.respect.datalayer.shared.WritableDataSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.params.GetListCommonParams

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
