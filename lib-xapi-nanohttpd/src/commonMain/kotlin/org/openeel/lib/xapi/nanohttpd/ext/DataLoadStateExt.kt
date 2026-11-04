package org.openeel.lib.xapi.nanohttpd.ext

import fi.iki.elonen.NanoHTTPD.Response
import fi.iki.elonen.NanoHTTPD.newFixedLengthResponse
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import org.openeel.lib.dataloadstate.DataErrorResult
import org.openeel.lib.dataloadstate.DataLoadingState
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.lib.dataloadstate.throwable.unwrapHttpStatusCode


suspend fun <T: Any> DataLoadState<T>.toFixedLengthResponse(
    json: Json,
    serializer: SerializationStrategy<T>
): Response  {
    return toFixedLengthResponse(
        dataReadyResponse = { dataReady ->
            newFixedLengthResponse(
                Response.Status.OK,
                "application/json",
                json.encodeToString(serializer, dataReady.data)
            )
        }
    )
}

/**
 * Note: still needs to handle not modified etc
 */
suspend fun <T: Any> DataLoadState<T>.toFixedLengthResponse(
    dataReadyResponse: suspend (DataReadyState<T>) -> Response,
): Response {
    return when(this) {
        is DataReadyState -> {
            dataReadyResponse(this).also { it.addHeaders(this.metaInfo.headers) }
        }

        is NoDataLoadedState -> {
            when(this.reason) {
                NoDataLoadedState.Reason.NOT_MODIFIED -> {
                    newFixedLengthResponse(
                        Response.Status.NOT_MODIFIED, "text/plain", ""
                    ).also { it.addHeaders(metaInfo.headers) }
                }
                NoDataLoadedState.Reason.NOT_FOUND -> {
                    newFixedLengthResponse(
                        Response.Status.NOT_FOUND, "text/plain", "not found"
                    ).also { it.addHeaders(metaInfo.headers) }
                }
            }
        }

        is DataErrorResult -> {
            val statusCode = this.error.unwrapHttpStatusCode() ?: 500
            val status = Response.Status.lookup(statusCode) ?: Response.Status.INTERNAL_ERROR
            newFixedLengthResponse(
                status, "text/plain", this.error.message ?: ""
            ).also { it.addHeaders(metaInfo.headers) }
        }

        is DataLoadingState -> {
            newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "loading")
        }
    }
}
