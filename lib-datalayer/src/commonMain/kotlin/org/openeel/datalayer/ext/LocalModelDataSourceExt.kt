package world.respect.datalayer.ext

import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.shared.LocalModelDataSource
import world.respect.lib.dataloadstate.ext.dataOrNull

@Suppress("unused")
suspend fun <T: Any> LocalModelDataSource<T>.updateFromRemoteListIfNeeded(
    remoteLoad: DataLoadState<List<T>>,
    validationHelper: ExtendedDataSourceValidationHelper?
) {
    val data: List<T>? = remoteLoad.dataOrNull()

    if(data != null) {
        updateLocal(data)
        validationHelper
            ?.takeIf { remoteLoad is DataReadyState }
            ?.updateValidationInfo(remoteLoad.metaInfo)
    }
}

suspend fun <T: Any> LocalModelDataSource<T>.updateFromRemoteIfNeeded(
    remoteLoad: DataLoadState<T>,
    validationHelper: ExtendedDataSourceValidationHelper?
) {
    val data = remoteLoad.dataOrNull()

    if(data != null) {
        updateLocal(listOf(data))
        validationHelper
            ?.takeIf { remoteLoad is DataReadyState }
            ?.updateValidationInfo(remoteLoad.metaInfo)
    }
}

