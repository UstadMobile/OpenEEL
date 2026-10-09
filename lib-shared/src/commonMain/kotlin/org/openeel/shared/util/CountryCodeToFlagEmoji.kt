package world.respect.shared.util

import io.github.aakira.napier.Napier

/**
 * Converts a country code to a flag emoji.
 *
 * A flag emoji is a pair of Unicode regional indicator symbols, one for each letter of the
 * ISO 3166-1 alpha-2 country code, e.g. "DE" becomes the indicator for D followed by the
 * indicator for E.
 *
 * References:
 * - Emoji flag sequences: https://unicode.org/reports/tr51/
 * - Regional indicator code chart: https://www.unicode.org/charts/PDF/U1F100.pdf
 * - Country codes: https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2
 *
 * @param countryCode Two-letter ISO 3166-1 alpha-2 country code (e.g., "DE", "US"), or null
 *        where the country is not known.
 * @return Flag emoji string, or an empty string where countryCode is null or is not a valid
 *         two-letter code.
 */
fun countryCodeToFlagEmoji(countryCode: String): String {
    val code = countryCode.uppercase()
    if (code.length != COUNTRY_CODE_LENGTH || !code.all { it in 'A'..'Z' }) {
        Napier.w("countryCodeToFlagEmoji: not a valid ISO 3166-1 alpha-2 country code: $countryCode")
        return ""
    }

    return buildString {
        code.forEach { letter ->
            @Suppress("KotlinArrayToString")
            append(Character.toChars(REGIONAL_INDICATOR_A + (letter - 'A')))
        }
    }
}

/** Code point for REGIONAL INDICATOR SYMBOL LETTER A. B, C, D etc follow sequentially.
 * 1F1E6 [A] REGIONAL INDICATOR SYMBOL LETTER A as per https://www.unicode.org/charts/PDF/U1F100.pdf
 **/
private const val REGIONAL_INDICATOR_A = 0x1F1E6

private const val COUNTRY_CODE_LENGTH = 2
