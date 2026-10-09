package world.respect.xapi.ipc.shared.messages.ext

import android.os.Bundle
import android.os.ParcelFileDescriptor
import io.ktor.http.Headers
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.runBlocking
import world.respect.lib.dataloadstate.DataErrorResult
import world.respect.lib.dataloadstate.DataLoadMetaInfo
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.lib.dataloadstate.throwable.HttpErrorResponseException
import world.respect.lib.xapi.model.XapiDocument
import world.respect.lib.xapi.model.XapiDocumentByteArrayImpl
import world.respect.xapi.ipc.shared.messages.XapiIpcKeys
import java.util.concurrent.ExecutorService


/**
 * Convert an [XapiDocument] to a Bundle that can be sent using the IPC. This uses a pipe to
 * send the contents of the document.
 */
fun XapiDocument.toBundle(
    executor: ExecutorService
): Bundle {
    val bundle = Bundle()

    val pipe = ParcelFileDescriptor.createPipe()

    //As per https://developer.android.com/reference/android/os/ParcelFileDescriptor#createPipe()
    val readSide = pipe[0]
    val writeSide = pipe[1]

    bundle.putParcelable(XapiIpcKeys.KEY_XAPI_DOC_FD, readSide)
    bundle.putString(XapiIpcKeys.KEY_XAPI_DOC_TYPE, type)
    bundle.putLong(XapiIpcKeys.KEY_XAPI_DOC_UPDATED, updated.timestamp)

    executor.submit {
        ParcelFileDescriptor.AutoCloseOutputStream(writeSide).use { parcelOut ->
            parcelOut.write(
                runBlocking { contentsAsByteArray() }
            )
        }
    }

    return bundle
}

/**
 * Convert a Bundle back to [XapiDocument] where the Docuemnt was converted to a XapiDocument by
 * toBundle
 */
fun Bundle.toXapiDocument(): XapiDocument {
    val fd = getParcelable<ParcelFileDescriptor>(XapiIpcKeys.KEY_XAPI_DOC_FD)
        ?: throw IllegalArgumentException("Missing ParcelFileDescriptor for XapiDocument")
    val type = getString(XapiIpcKeys.KEY_XAPI_DOC_TYPE)
        ?: throw IllegalArgumentException("Missing type for XapiDocument")
    val updatedTimestamp = getLong(XapiIpcKeys.KEY_XAPI_DOC_UPDATED)
    val updated = GMTDate(updatedTimestamp)

    val contents = ParcelFileDescriptor.AutoCloseInputStream(fd).use { input ->
        input.readBytes()
    }

    return XapiDocumentByteArrayImpl(
        type = type,
        updated = updated,
        contents = contents
    )
}

/**
 * Convert a [DataLoadState] of [XapiDocument] to a Bundle that can be sent using the IPC.
 */
fun DataLoadState<XapiDocument>.toBundle(
    executor: ExecutorService
): Bundle {
    return toBundle(
        putBody = { data ->
            putAll(data.data.toBundle(executor))
        }
    )
}

/**
 * Convert a Bundle back to a [DataLoadState] of [XapiDocument].
 */
fun Bundle.toXapiDocumentDataLoadState(): DataLoadState<XapiDocument> {
    return toDataLoadState(
        getBody = { it.toXapiDocument() }
    )
}
