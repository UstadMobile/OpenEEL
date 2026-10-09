package world.respect.lib.xapi.resources

import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.xapi.model.XapiActivity


/**
 * As per the Xapi spec:
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#25-activities-resource
 */
interface XapiActivitiesResource {

    suspend fun get(activityId: String): DataLoadState<XapiActivity>

}