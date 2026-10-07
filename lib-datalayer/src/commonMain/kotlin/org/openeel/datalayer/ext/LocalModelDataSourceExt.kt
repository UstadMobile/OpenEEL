package org.openeel.datalayer.ext

import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import org.openeel.datalayer.shared.LocalModelDataSource
import org.openeel.lib.dataloadstate.ext.dataOrNull

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

