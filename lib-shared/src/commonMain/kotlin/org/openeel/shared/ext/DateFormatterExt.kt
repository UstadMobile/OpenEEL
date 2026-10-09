package world.respect.shared.ext

import java.text.SimpleDateFormat
import java.util.Locale

fun String.toLocalizedDate(
    inputPattern: String = "yyyy-MM-dd",
    outputPattern: String = "dd/MM/yyyy",
    locale: Locale = Locale.getDefault()
): String {
    if (this.isBlank()) return ""
    return try {
        val inputFormat = SimpleDateFormat(inputPattern, locale)
        val outputFormat = SimpleDateFormat(outputPattern, locale)
        val date = inputFormat.parse(this)
        if (date != null) outputFormat.format(date) else this
    } catch (e: Exception) {
        this
    }
}
