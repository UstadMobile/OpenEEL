package world.respect.shared.navigation

import kotlinx.coroutines.flow.Flow

/**
 * NavResultReturner is responsible for "returning" results via the Navigation e.g. where one screen
 * returns a result to another screen such as when a user is creating an assignment and selects a
 * lesson to assign by browsing apps/lessons.
 *
 */
interface NavResultReturner {

    fun resultFlowForKey(key: String): Flow<NavResult>

    fun sendResult(result: NavResult)

}