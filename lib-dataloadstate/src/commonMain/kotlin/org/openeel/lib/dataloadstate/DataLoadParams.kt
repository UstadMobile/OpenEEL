package world.respect.lib.dataloadstate

import io.ktor.http.Headers

data class DataLoadParams(
    //Will be deprecated: should be handled in requestHeaders
    val mustRevalidate: Boolean = false,
    //Will be deprecated: should be handled using requestHeaders
    val onlyIfCached: Boolean = false,
    val requestHeaders: Headers = Headers.Empty,
)
