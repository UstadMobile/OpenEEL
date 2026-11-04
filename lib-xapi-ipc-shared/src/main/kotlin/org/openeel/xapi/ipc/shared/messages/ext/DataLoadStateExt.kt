package org.openeel.xapi.ipc.shared.messages.ext

import android.os.Bundle
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import org.openeel.lib.dataloadstate.DataErrorResult
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataLoadingState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.lib.dataloadstate.throwable.unwrapHttpStatusCode
import org.openeel.xapi.ipc.shared.messages.XapiIpcKeys

const val STATUS_LOADING = -2

fun <T: Any> DataLoadState<T>.toBundle(
    serializer: SerializationStrategy<T>,
    json: Json,
): Bundle {
    return toBundle { data ->
        putSerialized(
            key = XapiIpcKeys.KEY_BODY,
            json = json,
            serializer = serializer,
            value = data.data,
        )
    }
}

fun <T: Any> DataLoadState<T>.toBundle(
    putBody: Bundle.(DataReadyState<T>) -> Unit,
): Bundle {
    return when(this) {
        is DataReadyState<T> -> {
            Bundle().apply {
                putInt(XapiIpcKeys.KEY_STATUS_CODE, 200)
                putXapiIpcHeaders(metaInfo.headers)
                this@apply.putBody(this@toBundle)
            }
        }

        is DataLoadingState<T> -> {
            Bundle().apply {
                putInt(XapiIpcKeys.KEY_STATUS_CODE, STATUS_LOADING)
                putXapiIpcHeaders(metaInfo.headers)
            }
        }

        is DataErrorResult<T> -> {
            Bundle().also {
                it.putInt(XapiIpcKeys.KEY_STATUS_CODE, error.unwrapHttpStatusCode() ?: 500)
                it.putXapiIpcHeaders(metaInfo.headers)
            }
        }

        is NoDataLoadedState<T> -> {
            Bundle().also {
                it.putXapiIpcHeaders(metaInfo.headers)
                it.putInt(
                    XapiIpcKeys.KEY_STATUS_CODE,
                    if(reason == NoDataLoadedState.Reason.NOT_MODIFIED) {
                        302
                    }else {
                        404
                    }
                )
            }
        }

    }
}