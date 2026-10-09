package world.respect.lib.xapi.ext

import world.respect.lib.xapi.model.XapiContext
import world.respect.lib.xapi.model.XapiContextActivities

fun XapiContext.contextActivitiesOrBlank(): XapiContextActivities {
    return contextActivities ?: XapiContextActivities()
}