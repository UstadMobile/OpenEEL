package org.openeel.shared.ext

import io.github.aakira.napier.Napier
import org.openeel.shared.util.exception.getUiTextOrGeneric
import org.openeel.shared.viewmodel.app.appstate.Snack
import org.openeel.shared.viewmodel.app.appstate.SnackBarDispatcher

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
