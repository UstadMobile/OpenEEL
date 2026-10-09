package world.respect.server.util.ext

import io.ktor.http.HttpHeaders
import io.ktor.http.toHttpDate
import io.ktor.server.response.ApplicationResponse
import io.ktor.server.response.header
import io.ktor.util.date.GMTDate
import kotlin.time.Instant

/**
 * Set last modified header using kotlinx time Instant. This uses instant.epochSeconds * 1000
 * instead of instant.toEpochMillis because the last modified http date is accuate only to the
 * nearest second.
 */
fun ApplicationResponse.lastModified(instant: Instant) {
    val httpDateStr = GMTDate(instant.epochSeconds * 1_000).toHttpDate()
    header(HttpHeaders.LastModified,httpDateStr)
}
