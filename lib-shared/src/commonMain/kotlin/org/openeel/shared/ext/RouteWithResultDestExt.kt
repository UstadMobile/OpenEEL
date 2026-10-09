package world.respect.shared.ext

import world.respect.shared.navigation.RouteWithResultDest

/**
 * Shorthand: if true, then the user is in a navigation flow where a result is expected to be
 * returned (see NavResultReturner)
 */
val RouteWithResultDest.resultExpected: Boolean
    get() = resultDest != null

