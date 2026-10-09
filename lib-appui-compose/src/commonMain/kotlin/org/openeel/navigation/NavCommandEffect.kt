package world.respect.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.RespectComposeNavController

/**
 * The RespectViewModel provides a Flow of NavCommand(s) that can be collected by the navigation
 * system whilst the component is active.
 *
 * This avoids the ViewModel having any reference to the context, which in turn avoids memory leaks.
 *
 * This also avoids any possibility of the ViewModel triggering navigation for a screen which is not
 * active.
 */
@Composable
fun NavCommandEffect(
    navHostController: RespectComposeNavController,
    navCommandFlow: Flow<NavCommand>,
) {
    LaunchedEffect(navHostController) {
        navCommandFlow.collect { navCommand ->
            navHostController.onCollectNavCommand(navCommand)

        }
    }
}
