package world.respect.xapi.ipc.shared.messages.ext

import android.os.Bundle
import io.ktor.http.Headers
import io.ktor.http.Parameters
import io.ktor.util.StringValues
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import world.respect.lib.dataloadstate.DataErrorResult
import world.respect.lib.dataloadstate.DataLoadMetaInfo
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.lib.dataloadstate.throwable.HttpErrorResponseException
import world.respect.xapi.ipc.shared.messages.BundleHeaders
import world.respect.xapi.ipc.shared.messages.BundleParameters
import world.respect.xapi.ipc.shared.messages.XapiIpcKeys

fun <T: Any> Bundle.putSerialized(
    key: String,
    json: Json,
    serializer: SerializationStrategy<T>,
    value: T,
) {
    putString(key, json.encodeToString(serializer, value))
}

fun <T: Any> Bundle.getDeserialized(
    key: String,
    json: Json,
    deserializer: DeserializationStrategy<T>
): T? {
    return getString(key)?.let {
        json.decodeFromString(deserializer, it)
    }
}

private const val SUFFIX_STR_VALS_CASE_INSENSITIVE = "_caseInsensitive"

/**
 * Put a set of StringValues into the given bundle, that can then be retrieved using
 * Bundle.getStringValues
 *
 * @param key Bundle key to use
 * @parma value StringValues to put into the bundle
 */
fun Bundle.putStringValues(
    key: String,
    value: StringValues
) {
    putBundle(key, value.toBundle())
    putBoolean(key + SUFFIX_STR_VALS_CASE_INSENSITIVE, value.caseInsensitiveName)
}

/**
 * Shorthand to put the query parameters into a sub-bundle with the standard key
 */
fun Bundle.putXapiIpcQueryParameters(
    queryParams: StringValues
) {
    putStringValues(key = XapiIpcKeys.KEY_QUERY_PARAMS, value = queryParams)
}

/**
 * Shorthand to retrieve the query parameters from a sub bundle with the standard key
 */
fun Bundle.getXapiIpcQueryParameters(): Parameters? {
    return BundleParameters(
        bundle = getBundle(XapiIpcKeys.KEY_QUERY_PARAMS) ?: return null
    )
}

fun Bundle.putXapiIpcHeaders(headers: Headers) {
    putStringValues(key = XapiIpcKeys.KEY_HEADERS, value = headers)
}

fun Bundle.getXapiIpcHeaders(): Headers? {
    return BundleHeaders(
        getBundle(XapiIpcKeys.KEY_HEADERS) ?: return null
    )
}

/**
 * Shorthand to get the dataload parameters. By default this will simply return a DataLoadParams
 * object with the headers set from getXapiIpcHeaders
 */
fun Bundle.getDataLoadParams(): DataLoadParams {
    return DataLoadParams(
        requestHeaders = getXapiIpcHeaders().orEmpty()
    )
}


fun Bundle.putAllFromStringMap(map: Map<String, String>) {
    map.forEach { (key, value) ->
        putString(key, value)
    }
}

fun <T: Any> Bundle.toDataLoadState(
    getBody: (Bundle) -> T
): DataLoadState<T> {
    val status = getInt(XapiIpcKeys.KEY_STATUS_CODE)

    return try {
        val metaInfo = DataLoadMetaInfo(
            headers = getXapiIpcHeaders().orEmpty(),
        )

        when(status) {
            200 -> {
                DataReadyState(
                    data = getBody(this),
                    metaInfo = metaInfo,
                )
            }

            302, 404 -> {
                NoDataLoadedState(
                    reason = NoDataLoadedState.Reason.forStatusCode(status),
                    metaInfo = metaInfo,
                )
            }

            else -> {
                DataErrorResult(
                    error = HttpErrorResponseException(status, "HTTP $status"),
                    metaInfo = metaInfo,
                )
            }
        }
    }catch (e: Throwable) {
        return DataErrorResult(e)
    }
}

fun <T: Any> Bundle.toDataLoadState(
    json: Json,
    deserializer: DeserializationStrategy<T>
): DataLoadState<T> {
    return toDataLoadState(
        getBody = {
            it.getDeserialized(
                key = XapiIpcKeys.KEY_BODY,
                json = json,
                deserializer = deserializer,
            ) ?: throw IllegalStateException("200 response has no body")
        }
    )
}
