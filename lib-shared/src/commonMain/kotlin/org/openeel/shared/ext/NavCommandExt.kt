package world.respect.shared.ext

import world.respect.shared.navigation.NavCommand
import kotlin.Boolean

fun NavCommand.withClearBackstack(
    clearBack: Boolean
): NavCommand {
    return when(this) {
        is NavCommand.Navigate -> NavCommand.Navigate(
            timestamp = timestamp,
            destination = destination,
            clearBackStack = clearBack,
            popUpTo = popUpTo,
            popUpToClass = popUpToClass,
            popUpToInclusive = popUpToInclusive,
        )
        else -> this
    }
}