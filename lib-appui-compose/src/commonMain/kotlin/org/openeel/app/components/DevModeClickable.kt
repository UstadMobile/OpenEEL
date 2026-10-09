package world.respect.app.components

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.koin.compose.getKoin
import world.respect.shared.domain.devmode.SetDevModeEnabledUseCase
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.app.appstate.Snack
import world.respect.shared.viewmodel.app.appstate.SnackBarDispatcher
import kotlin.compareTo
import kotlin.inc

@Composable
fun Modifier.clickableToEnableDevMode(
    onDevModeEnabled: () -> Unit = { },
): Modifier {
    var clickCount by remember { mutableStateOf(0) }
    val koin = getKoin()

    return clickable {
        clickCount++
        if(clickCount == 7) {
            val setDevModeEnabledUseCase: SetDevModeEnabledUseCase = koin.get()
            val snackDispatcher: SnackBarDispatcher = koin.get()

            setDevModeEnabledUseCase(true)
            onDevModeEnabled()
            snackDispatcher.showSnackBar(Snack("Developer mode enabled".asUiText()))
        }else if(clickCount > 7){
            val snackDispatcher: SnackBarDispatcher = koin.get()
            snackDispatcher.showSnackBar(Snack("Developer mode already enabled".asUiText()))
        }
    }
}