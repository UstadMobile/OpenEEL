package world.respect.lib.xapi.resources.local

import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.resources.XapiActivitiesResource
import kotlin.time.Instant

/**
 * The xAPI spec itself does not define an Activity as writable. Only it's canonical definition can
 * be updated. The XapiActivityDataSourceLocal will be used and required by the StatementDataSource
 * to handle updating canonical definitions
 */
interface XapiActivitiesResourceLocal: XapiActivitiesResource {

    /**
     * Update the canonical definition of activities as per xAPI spec rules
     *
     * @param activities list of activities to update
     * @param timestamp the timestamp of the update e.g. when definition updates are based on
     *        incoming statements, the timestamp of the statement.
     */
    suspend fun updateLocal(
        activities: List<XapiActivity>,
        timestamp: Instant,
    )

}