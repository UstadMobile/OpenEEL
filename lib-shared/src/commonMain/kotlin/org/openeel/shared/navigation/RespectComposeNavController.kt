package world.respect.shared.navigation

import androidx.navigation.NavHostController
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Wrapper that avoids accidental 'replay' of navigation commands.
 */
class RespectComposeNavController(
    private val navHostController: NavHostController,
) {

    @OptIn(ExperimentalTime::class)
    @Volatile
    private var lastNavCommandTime = Clock.System.now().toEpochMilliseconds()

    fun onCollectNavCommand(
        navCommand: NavCommand,
    ) {
        println("acknowledgement init"+navCommand.timestamp +" "+lastNavCommandTime)

        if(navCommand.timestamp <= lastNavCommandTime)
            return

        when (navCommand) {
            is NavCommand.Navigate -> {
                lastNavCommandTime = navCommand.timestamp
                if (navCommand.clearBackStack) {
                    navHostController.navigate(navCommand.destination) {
                        popUpTo(0) { inclusive = true }
                    }
                } else {
                    navHostController.navigate(navCommand.destination) {
                        val popUpToRoute = navCommand.popUpTo
                        val popUpToClass = navCommand.popUpToClass

                        when {
                            popUpToRoute != null -> {
                                popUpTo(popUpToRoute) { inclusive = navCommand.popUpToInclusive }
                            }
                            popUpToClass != null -> {
                                popUpTo(route = popUpToClass) { inclusive = navCommand.popUpToInclusive }
                            }
                        }
                    }
                }
            }

            is NavCommand.PopToRoute -> {
                lastNavCommandTime = navCommand.timestamp
                navHostController.popBackStack(
                    navCommand.destination, navCommand.inclusive
                )
            }

            is NavCommand.PopToRouteClass -> {
                lastNavCommandTime = navCommand.timestamp
                navHostController.popBackStack(
                    route = navCommand.destination, inclusive = navCommand.inclusive
                )
            }

            is NavCommand.PopUp -> {
                lastNavCommandTime = navCommand.timestamp
                navHostController.popBackStack()
            }
        }
    }

}