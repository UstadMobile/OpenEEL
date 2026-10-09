package world.respect.lib.dataloadstate.ktorserver

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.fromHttpToGmtDate
import io.ktor.http.toHttpDate
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.util.date.GMTDate
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.typeInfo
import world.respect.lib.dataloadstate.DataErrorResult
import world.respect.lib.dataloadstate.DataLayerHeaders
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.lib.dataloadstate.ext.etagForHttpResponseHeader
import world.respect.lib.dataloadstate.ext.lastModifiedForHttpResponseHeader
import world.respect.lib.dataloadstate.throwable.ExceptionWithHttpStatusCode
import kotlin.time.Instant


/**
 * Handles a response given a DataLoadState. This function will:
 *
 * a) Check incoming cache-validation headers: if-none-match and if-not-modified-since. If the
 *    validation passes, then will respond 302 not modified.
 * b) Add dataloadstate metadata headers to the response: Last-Modified, X-Consistent-Through,
 *    and etag.
 * c) If dataloadstate is NoDataLoaded with the reason not found, then will respond 404
 * d) If dataloadstate is DataReadyState then will respond with the data.
 *
 * @param dataLoadState the dataloadstate
 * @param typeInfo TypeInfo for the response as per ApplicationCall.respond
 */
suspend fun <T: Any> ApplicationCall.respondDataLoadState(
    dataLoadState: DataLoadState<T>,
    typeInfo: TypeInfo,
) {
    respondDataLoadState(
        dataLoadState = dataLoadState,
        onRespondWithData = { data ->
            this.respond(data, typeInfo)
        }
    )
}

/**
 * Handles a response given a DataLoadState, uses the default TypeInfo as per type parameter T.
 * This function will:
 *
 * a) Check incoming cache-validation headers: if-none-match and if-not-modified-since. If the
 *    validation passes, then will respond 302 not modified.
 * b) Add dataloadstate metadata headers to the response: Last-Modified, X-Consistent-Through,
 *    and etag.
 * c) If dataloadstate is NoDataLoaded with the reason not found, then will respond 404
 * d) If dataloadstate is DataReadyState then will respond with the data.
 *
 * @param dataLoadState the dataloadstate
 * @param T TypeInfo for the response
 */
suspend inline fun <reified T: Any> ApplicationCall.respondDataLoadState(
    dataLoadState: DataLoadState<T>,
) {
    respondDataLoadState(
        dataLoadState = dataLoadState,
        typeInfo = typeInfo<T>()
    )
}

/**
 * Handles a response given a DataLoadState. This function will:
 * a) Check incoming cache-validation headers: if-none-match and if-not-modified-since. If the
 *    validation passes, then will respond 302 not modified.
 * b) Add dataloadstate metadata headers to the response: Last-Modified, X-Consistent-Through,
 *    and etag.
 * c) If dataloadstate is NoDataLoaded with the reason not found, then will respond 404
 * d) If dataloadstate is DataReadyState then will respond with the data.
 *
 * @param dataLoadState the dataloadstate
 * @param onRespondWithData function to respond with the data. By default use the typeInfo. A custom
 *        onRespond function might not use the typeInfo at all.
 */
suspend fun <T: Any> ApplicationCall.respondDataLoadState(
    dataLoadState: DataLoadState<T>,
    onRespondWithData: suspend ApplicationCall.(T) -> Unit,
) {
    dataLoadState.etagForHttpResponseHeader()?.also {
        response.header(HttpHeaders.ETag, it)
    }

    dataLoadState.lastModifiedForHttpResponseHeader()?.also {
        response.header(HttpHeaders.LastModified, it)
    }

    dataLoadState.metaInfo.consistentThrough?.also { consistentThrough ->
        response.header(
            name = DataLayerHeaders.XConsistentThrough,
            value = consistentThrough.toString()
        )
    }

    dataLoadState.metaInfo.permissionsLastModified?.also { permissionsLastMod ->
        response.header(
            name = DataLayerHeaders.XPermissionsLastModified,
            value = permissionsLastMod.toString()
        )
    }

    when {
        dataLoadState is DataReadyState -> {
            onRespondWithData(dataLoadState.data)
        }

        dataLoadState is NoDataLoadedState && dataLoadState.reason == NoDataLoadedState.Reason.NOT_MODIFIED -> {
            respond(HttpStatusCode.NotModified)
        }

        dataLoadState is NoDataLoadedState && dataLoadState.reason == NoDataLoadedState.Reason.NOT_FOUND -> {
            respond(HttpStatusCode.NotFound)
        }

        dataLoadState is DataErrorResult -> {
            respond(
                HttpStatusCode.fromValue(
                    (dataLoadState.error as? ExceptionWithHttpStatusCode)?.statusCode ?: 500
                )
            )
        }

        else -> {
            respond(HttpStatusCode.ServiceUnavailable)
        }
    }
}


/**
 * The If-Modified-Since header is really only accurate to the nearest second, so we need to convert.
 * Because If-Modified-Since is ONLY used on request headers, it makes sense to put this as an
 * extension function here.
 */
fun ApplicationRequest.ifModifiedSinceSecs(): Long? {
    return call.request.headers[HttpHeaders.IfModifiedSince]?.fromHttpToGmtDate()?.timestamp?.let {
        it / 1_000
    }
}



/**
 * Shorthand to determine if the request has not been modified based on the If-Modified-Since header
 * from the request and when the underlying response data was stored (see README.md for discussion
 * of difference between last modified time and stored time).
 *
 * @param responseDataLastModified the time data was last modified in seconds since epoch
 */
fun ApplicationRequest.validateIfNotModifiedSince(
    responseDataLastModified: Instant,
) : Boolean {
    return ifModifiedSinceSecs()?.let { ifModifiedSinceSecs ->
        responseDataLastModified.epochSeconds <= ifModifiedSinceSecs
    } ?: false
}

