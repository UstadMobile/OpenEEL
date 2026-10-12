package org.openeel.lib.xapi.resources

import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.xapi.model.XapiActivity


/**
 * As per the Xapi spec:
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#25-activities-resource
 */
interface XapiActivitiesResource {

    suspend fun get(activityId: String): DataLoadState<XapiActivity>

}