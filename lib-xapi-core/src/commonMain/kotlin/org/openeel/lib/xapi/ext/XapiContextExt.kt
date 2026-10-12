package org.openeel.lib.xapi.ext

import org.openeel.lib.xapi.model.XapiContext
import org.openeel.lib.xapi.model.XapiContextActivities

fun XapiContext.contextActivitiesOrBlank(): XapiContextActivities {
    return contextActivities ?: XapiContextActivities()
}