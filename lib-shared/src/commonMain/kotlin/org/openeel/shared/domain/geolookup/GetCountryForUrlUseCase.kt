package world.respect.shared.domain.geolookup

import io.ktor.http.Url

/**
 * Gets the country code for a given URL by looking up the location of the server hosting it.
 */
interface GetCountryForUrlUseCase {

    /**
     * Gets the country code for a URL. The URL can be any URL - only the host is used.
     *
     * @param url The full URL (e.g., "https://onrespect.app/")
     * @return Two-letter ISO 3166-1 alpha-2 country code (e.g., "DE", "US") or null if the
     *         country cannot be determined e.g. where the host is on a private network.
     */
    suspend operator fun invoke(url: Url): String?
}