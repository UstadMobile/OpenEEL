package world.respect.xapi.ipc.client.ext

import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import org.openeel.lib.ipc.messagebridge.MessageData
import world.respect.lib.xapi.exceptions.XapiException
import world.respect.xapi.ipc.shared.messages.XapiIpcKeys

/**
 * Check the status code in the message data. If the status is not successful, throw a [XapiException]
 *
 * @receiver MessageData where the bundle contains the status code in XapiIpcKeys.KEY_STATUS_CODE
 * @return the same MessageData instance
 */
fun MessageData.throwXapiExceptionIfStatusNotSuccessful(): MessageData {
    val status = HttpStatusCode.fromValue(data.getInt(XapiIpcKeys.KEY_STATUS_CODE))
    if(!status.isSuccess())
        throw XapiException(status.value, status.description)

    return this
}
