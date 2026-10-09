package org.openeel.datalayer.school

import io.ktor.http.Parameters
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.datalayer.school.model.PersonQrBadge
import org.openeel.datalayer.shared.WritableDataSource
import org.openeel.datalayer.shared.params.GetListCommonParams

interface PersonQrBadgeDataSource : WritableDataSource<PersonQrBadge> {

    data class GetListParams(
        val common: GetListCommonParams = GetListCommonParams(),
        val qrCodeUrl: Url? = null,
    ) {
        companion object {
            fun fromParams(params: Parameters): GetListParams {
                return GetListParams(
                    common = GetListCommonParams.fromParams(params),
                    qrCodeUrl = params[PARAM_QRCODE_URL]?.let { Url(it) },
                )
            }
        }
    }

    suspend fun listAll(
        loadParams: DataLoadParams,
        listParams: GetListParams = GetListParams(),
    ): DataLoadState<List<PersonQrBadge>>

    fun listAllAsFlow(
        loadParams: DataLoadParams = DataLoadParams(),
        listParams: GetListParams = GetListParams(),
    ): Flow<DataLoadState<List<PersonQrBadge>>>

    fun findByGuidAsFlow(
        loadParams: DataLoadParams,
        guid: String
    ): Flow<DataLoadState<PersonQrBadge>>


    companion object {

        const val ENDPOINT_NAME = "personqrbadge"

        const val PARAM_QRCODE_URL = "qrCodeUrl"

    }
}