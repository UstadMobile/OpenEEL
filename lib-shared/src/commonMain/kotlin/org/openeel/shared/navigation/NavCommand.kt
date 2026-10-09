package world.respect.shared.navigation

import world.respect.libutil.util.time.systemTimeInMillis
import kotlin.reflect.KClass

sealed class NavCommand(
    val timestamp: Long = systemTimeInMillis(),
) {
    class Navigate(
        val destination: RespectAppRoute,
        val clearBackStack: Boolean = false,
        val popUpTo: RespectAppRoute? = null,
        val popUpToClass: KClass<*>? = null,
        val popUpToInclusive: Boolean = false,
        timestamp: Long = systemTimeInMillis(),
    ) : NavCommand(timestamp)

    class PopToRoute(
        val destination: RespectAppRoute,
        val inclusive: Boolean,
        timestamp: Long = systemTimeInMillis(),
    ): NavCommand(timestamp)

    class PopToRouteClass(
        val destination: KClass<*>,
        val inclusive: Boolean,
        timestamp: Long = systemTimeInMillis(),
    ): NavCommand(timestamp)

    class PopUp(
        timestamp: Long = systemTimeInMillis(),
    ): NavCommand(timestamp)


}

