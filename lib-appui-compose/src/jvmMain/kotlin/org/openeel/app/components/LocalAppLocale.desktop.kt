package world.respect.app.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale
import java.util.Locale.*

actual object LocalAppLocale {
    private var default: Locale? = null
    private val LocalAppLocale = staticCompositionLocalOf { getDefault().toString() }
    actual val current: String
        @Composable get() = LocalAppLocale.current

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        if (default == null) {
            default = getDefault()
        }
        val new = when(value) {
            null -> default!!
            else -> Locale(value)
        }
        setDefault(new)
        return LocalAppLocale.provides(new.toString())
    }
}