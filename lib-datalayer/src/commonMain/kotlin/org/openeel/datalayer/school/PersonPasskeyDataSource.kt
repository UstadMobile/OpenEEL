package world.respect.datalayer.school

import kotlinx.coroutines.flow.Flow
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.datalayer.school.model.PersonPasskey
import world.respect.datalayer.shared.WritableDataSource

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