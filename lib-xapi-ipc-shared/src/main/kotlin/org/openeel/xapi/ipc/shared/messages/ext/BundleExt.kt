package org.openeel.xapi.ipc.shared.messages.ext

import android.os.Bundle
import io.ktor.http.Headers
import io.ktor.util.StringValues
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import org.openeel.lib.dataloadstate.DataErrorResult
import org.openeel.lib.dataloadstate.DataLoadMetaInfo
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.xapi.ipc.shared.messages.XapiIpcKeys

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
 * Get a set of StringValues from the given bundle (that was stored using putStringValues).
 *
 * @param key Bundle key to use
 * @return The StringValues, or null if not found
 */
fun Bundle.getStringValues(
    key: String
): StringValues? {
    val bundle = getBundle(key) ?: return null
    val caseInsensitive = getBoolean(key + SUFFIX_STR_VALS_CASE_INSENSITIVE)
    return BundleStringValues(bundle, caseInsensitive)
}

fun Bundle.putAllFromStringMap(map: Map<String, String>) {
    map.forEach { (key, value) ->
        putString(key, value)
    }
}

fun <T: Any> Bundle.toDataLoadState(
    json: Json,
    deserializer: DeserializationStrategy<T>
): DataLoadState<T> {
    val status = getInt(XapiIpcKeys.KEY_STATUS_CODE)

    return try {
        val metaInfo = DataLoadMetaInfo(
            headers = Headers.build {
                this@toDataLoadState.getStringValues(XapiIpcKeys.KEY_HEADERS)?.also { stringVals ->
                    appendAll(stringVals)
                }
            }
        )

        when(status) {
            200 -> {
                DataReadyState(
                    data = getDeserialized(
                        key = XapiIpcKeys.KEY_BODY,
                        json = json,
                        deserializer = deserializer,
                    ) ?: throw IllegalStateException("200 response has no body"),
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
                    error = IllegalStateException()
                )
            }
        }
    }catch (e: Throwable) {
        return DataErrorResult(e)
    }
}
