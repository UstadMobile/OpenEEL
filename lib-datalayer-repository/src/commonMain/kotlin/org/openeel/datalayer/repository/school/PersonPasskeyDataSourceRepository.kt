package world.respect.datalayer.repository.school

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.ext.combineWithRemote
import world.respect.datalayer.ext.updateFromRemoteListIfNeeded
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.school.PersonPasskeyDataSource
import world.respect.datalayer.school.PersonPasskeyDataSourceLocal
import world.respect.datalayer.school.model.PersonPasskey

class PersonPasskeyDataSourceRepository(
    val local: PersonPasskeyDataSourceLocal,
    val remote: PersonPasskeyDataSource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
): PersonPasskeyDataSource {

    override suspend fun listAll(
        listParams: PersonPasskeyDataSource.GetListParams
    ): DataLoadState<List<PersonPasskey>> {
        val remoteResult = remote.listAll(listParams.copy(includeRevoked = true))
        local.updateFromRemoteListIfNeeded(remoteResult, validationHelper)

        return local.listAll(listParams = listParams)
    }

    override fun listAllAsFlow(
        listParams: PersonPasskeyDataSource.GetListParams
    ): Flow<DataLoadState<List<PersonPasskey>>> {
        return local.listAllAsFlow(listParams = listParams).combineWithRemote(
            remoteFlow = remote.listAllAsFlow(
                listParams.copy(includeRevoked = true)
            ).onEach {
                local.updateFromRemoteListIfNeeded(it, validationHelper)
            }
        )
    }

    /**
     * Note: Passkeys are only updated online.
     */
    override suspend fun store(list: List<PersonPasskey>) {
        remote.store(list)
        local.store(list)
    }

}