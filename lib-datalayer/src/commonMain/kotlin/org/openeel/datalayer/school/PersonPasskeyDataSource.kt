package org.openeel.datalayer.school

import kotlinx.coroutines.flow.Flow
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.datalayer.school.model.PersonPasskey
import org.openeel.datalayer.shared.WritableDataSource

interface PersonPasskeyDataSource: WritableDataSource<PersonPasskey> {

    data class GetListParams(
        val includeRevoked: Boolean = false,
    )

    suspend fun listAll(
        listParams: GetListParams = GetListParams(),
    ): DataLoadState<List<PersonPasskey>>

    fun listAllAsFlow(
        listParams: GetListParams = GetListParams(),
    ): Flow<DataLoadState<List<PersonPasskey>>>


    companion object {

        const val ENDPOINT_NAME = "personpasskey"

        const val PARAM_INCLUDE_REVOKED = "includeRevoked"

    }

}