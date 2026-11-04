package org.openeel.datalayer.school

import io.ktor.http.Parameters
import kotlinx.coroutines.flow.Flow
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.datalayer.school.model.PersonPassword
import org.openeel.datalayer.shared.WritableDataSource
import org.openeel.datalayer.shared.params.GetListCommonParams

interface PersonPasswordDataSource: WritableDataSource<PersonPassword> {

    data class GetListParams(
        val common: GetListCommonParams = GetListCommonParams(),
    ) {
        companion object {
            fun fromParams(params: Parameters): GetListParams {
                return GetListParams(
                    common = GetListCommonParams.fromParams(params)
                )
            }
        }
    }

    suspend fun listAll(
        listParams: GetListParams = GetListParams(),
    ): DataLoadState<List<PersonPassword>>

    fun listAllAsFlow(
        loadParams: DataLoadParams = DataLoadParams(),
        listParams: GetListParams = GetListParams(),
    ): Flow<DataLoadState<List<PersonPassword>>>

    companion object {

        const val ENDPOINT_NAME = "personpassword"

    }

}