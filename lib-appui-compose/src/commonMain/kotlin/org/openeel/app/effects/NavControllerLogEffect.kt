package world.respect.app.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import world.respect.libutil.util.putDebugCrashCustomData

@Composable
fun NavControllerLogEffect(
    navController: NavController,
) {
    val currentBackStackEntry by navController.currentBackStackEntryFlow.collectAsState(null)
    LaunchedEffect(currentBackStackEntry) {
        val argsString = currentBackStackEntry?.destination?.arguments?.map {
            it.key to it.value.toString()
        }?.joinToString { "${it.first}=${it.second}" }

        putDebugCrashCustomData("currentbackstackentry",
            buildString {
                append("Route=")
                append(currentBackStackEntry?.destination?.route ?: "unknown")
                append(" Args=${argsString}")
            }
        )
    }
}
