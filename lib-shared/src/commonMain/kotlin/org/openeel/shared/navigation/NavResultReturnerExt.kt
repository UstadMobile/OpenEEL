package world.respect.shared.navigation

import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Send a result back using NavResultReturner and pop the navigatoin stack if the given route
 * expects a result to be sent back.
 *
 * @return true if a result was sent, false otherwise
 */
fun NavResultReturner.sendResultIfResultExpected(
    route: RouteWithResultDest,
    navCommandFlow: MutableSharedFlow<NavCommand>,
    result: Any?
): Boolean {
    val resultDest = route.resultDest

    val resultKey = route.resultDest?.resultKey

    return if(resultKey != null) {
        sendResult(
            NavResult(
                key = resultKey,
                result = result,
            )
        )

        val command = when (resultDest) {
            is KClassResultDest -> {
                NavCommand.PopToRouteClass(
                    destination = resultDest.resultPopUpTo,
                    inclusive = false,
                )
            }

            is RouteResultDest -> {
                NavCommand.PopToRoute(
                    destination = resultDest.resultPopUpTo,
                    inclusive = false,
                )
            }

            else -> {
                null
            }
        }

        if(command != null) {
            navCommandFlow.tryEmit(command)
        }

        true
    }else {
        false
    }
}