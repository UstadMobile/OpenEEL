package world.respect.shared.ext

import io.github.aakira.napier.Napier
import world.respect.shared.util.exception.getUiTextOrGeneric
import world.respect.shared.viewmodel.app.appstate.Snack
import world.respect.shared.viewmodel.app.appstate.SnackBarDispatcher

suspend fun SnackBarDispatcher.tryOrShowSnackbarOnError(
    logMessage: String = "tryOrShowSnackbarOnError",
    block: suspend () -> Unit,
) {
    try {
        block()
    }catch(e: Throwable) {
        Napier.e(message = logMessage, throwable = e)
        showSnackBar(Snack(e.getUiTextOrGeneric()))
    }
}
