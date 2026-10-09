package world.respect.app.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import org.koin.compose.koinInject
import world.respect.shared.domain.geolookup.GetCountryForUrlUseCase
import world.respect.shared.util.countryCodeToFlagEmoji

@Composable
fun rememberCountryFlagEmoji(url: Url): String? {
    val getCountryForUrlUseCase: GetCountryForUrlUseCase = koinInject()

    var flagEmojiStr by remember(url) { mutableStateOf<String?>(null) }

    LaunchedEffect(url) {
        flagEmojiStr = try {
            getCountryForUrlUseCase(url)?.let { countryCodeToFlagEmoji(it) }
        } catch (e: Exception) {
            Napier.w("CountryFlag: could not get country for $url", e)
            null
        }
    }

    return flagEmojiStr
}