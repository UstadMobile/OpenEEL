package world.respect.shared.domain.geolookup

import io.github.aakira.napier.Napier
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.Url
import io.ktor.http.encodeURLParameter
import kotlinx.serialization.Serializable

/**
 * Gets the country for a URL using an IP address country lookup server.
 *
 * There is deliberately no cache here: the HttpClient uses UstadCacheInterceptor, so responses
 * are cached by the HTTP cache as per the Cache-Control header set by the lookup server.
 *
 * The server implements the same JSON API format as ip-api.com.
 * Reference: https://ip-api.com/docs/api:json
 *
 * @param geolocationEndpoint Base URL of the lookup server, as per the
 *        GEOLOCATION_API_ENDPOINT build environment variable.
 */
class GetCountryForUrlUseCaseImpl(
    private val httpClient: HttpClient,
    private val geolocationEndpoint: Url,
) : GetCountryForUrlUseCase {

    private val cache = Cache.Builder<String, CountryResponse>().build()

    override suspend operator fun invoke(url: Url): String? {
        val host = url.host

        if(cache.get(host)?.status != STATUS_SUCCESS)
            cache.invalidate(host)

        return cache.get(host) {
            val encodedHost = host.encodeURLParameter()
            try {
                httpClient.get("$geolocationEndpoint/json/$encodedHost").body()
            } catch (e: Throwable) {
                Napier.w("CountryFlag: could not get country for $url", e)
                CountryResponse(status = "error")
            }
        }.takeIf { it.status == STATUS_SUCCESS }?.countryCode
    }

    companion object {
        private const val STATUS_SUCCESS = "success"
    }

    @Serializable
    private data class CountryResponse(
        val status: String,
        val countryCode: String? = null,
        val country: String? = null,
        val message: String? = null,
        val query: String? = null
    )
}