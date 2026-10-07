package org.openeel.datalayer.repository.school

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.ext.combineWithRemote
import org.openeel.datalayer.ext.updateFromRemoteListIfNeeded
import org.openeel.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import org.openeel.datalayer.school.PersonPasskeyDataSource
import org.openeel.datalayer.school.PersonPasskeyDataSourceLocal
import org.openeel.datalayer.school.model.PersonPasskey

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