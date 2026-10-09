package world.respect.datalayer.http.ext

import io.ktor.http.ParametersBuilder
import world.respect.lib.dataloadstate.DataLayerParams
import world.respect.datalayer.shared.params.GetListCommonParams
import kotlin.uuid.Uuid

fun ParametersBuilder.appendIfNotNull(
    name: String,
    value: String?
) {
    value?.also { append(name, it) }
}


fun ParametersBuilder.appendIfNotNull(
    name: String,
    value: Uuid?
) {
    value?.also { append(name, it.toString()) }
}

fun ParametersBuilder.appendCommonListParams(
    params: GetListCommonParams
) {
    appendIfNotNull(DataLayerParams.GUID, params.guid)
    appendIfNotNull(DataLayerParams.SINCE, params.since?.toString())
    appendIfNotNull(DataLayerParams.SINCE_IF_PERMISSIONS_NOT_CHANGED_SINCE,
        params.sinceIfPermissionsNotChangedSince?.toString())
    appendIfNotNull(DataLayerParams.SEARCH_QUERY, params.searchQuery)
    appendIfNotNull(DataLayerParams.INCLUDE_DELETED, params.includeDeleted?.toString())
}
