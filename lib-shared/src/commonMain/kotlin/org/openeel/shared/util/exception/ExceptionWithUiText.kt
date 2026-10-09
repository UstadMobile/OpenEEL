package world.respect.shared.util.exception

import kotlinx.io.IOException
import world.respect.libutil.ext.getCauseOfType
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.something_went_wrong
import world.respect.shared.generated.resources.network_error_check_try_again
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText

/**
 * An exception that has a (potentially localizable) UiText associated with it. This makes it easier
 * for ViewModels to show an appropriate error message to the user.
 */
interface ExceptionWithUiMessage {
    val uiText: UiText
}

class ExceptionUiMessageWrapper internal constructor(
    cause: Throwable?,
    message: String?,
    override val uiText: UiText
): Exception(message, cause), ExceptionWithUiMessage

@Suppress("unused")
fun Throwable.withUiText(uiText: UiText): Exception {
    return ExceptionUiMessageWrapper(this, message, uiText)
}

fun Throwable.getUiText(): UiText? {
    return getCauseOfType<ExceptionWithUiMessage>()?.uiText
}

fun Throwable.isIoException(): Boolean {
    return getCauseOfType<IOException>() != null
}

fun Throwable.getUiTextOrGeneric(): UiText {
    val uiText = getUiText()
    return when {
        uiText != null -> uiText
        isIoException() -> Res.string.network_error_check_try_again.asUiText()
        else -> Res.string.something_went_wrong.asUiText()
    }
}
