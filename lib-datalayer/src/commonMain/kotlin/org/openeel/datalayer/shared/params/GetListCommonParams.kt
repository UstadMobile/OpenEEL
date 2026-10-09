package world.respect.datalayer.shared.params

import io.ktor.util.StringValues
import world.respect.lib.dataloadstate.DataLayerParams
import kotlin.time.Instant

/**
 * Common parameters used on most RESPECT-ext get list endpoints
 *
 * @param includeDeleted if non-null and true, then entities where status is set to TO_BE_DELETED are
 *        included, otherwise false
 * @param since include only those entities that have been stored since the given instant (exclusive).
 * @param sinceIfPermissionsNotChangedSince if not null, then only use the since parameter on the
 *        condition that the authenticated users permissions have not changed since the given
 *        timestamp. This is a similar concept to the If-Range header. See PullSyncTracker doc for
 *        details.
 */
data class GetListCommonParams(
    val guid: String? = null,
    val searchQuery: String? = null,
    val since: Instant? = null,
    val sinceIfPermissionsNotChangedSince: Instant? = null,
    val includeDeleted: Boolean? = null,
) {

    companion object {

        fun fromParams(stringValues: StringValues) : GetListCommonParams{
            return GetListCommonParams(
                guid = stringValues[DataLayerParams.GUID],
                searchQuery = stringValues[DataLayerParams.SEARCH_QUERY],
                since = stringValues[DataLayerParams.SINCE]?.let { Instant.parse(it) },
                sinceIfPermissionsNotChangedSince = stringValues[DataLayerParams.SINCE_IF_PERMISSIONS_NOT_CHANGED_SINCE]
                    ?.let { Instant.parse(it) },
                includeDeleted = stringValues[DataLayerParams.INCLUDE_DELETED]?.toBoolean(),
            )
        }

    }

}