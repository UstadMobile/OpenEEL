package world.respect.xapi.ipc.shared.messages.ext

import android.os.Bundle
import android.os.Message
import android.os.Messenger
import kotlinx.serialization.builtins.serializer
import world.respect.lib.dataloadstate.DataErrorResult
import world.respect.lib.dataloadstate.throwable.unwrapHttpStatusCode
import world.respect.xapi.ipc.shared.messages.XapiIpcKeys

/**
 * Shorthand function to send a response message back to a client. Will obtain a new Message using
 * [Message.obtain] and send it using the receiver [Messenger].
 *
 * @receiver [Messenger] to use e.g. the [Message.replyTo] field from the message that was received
 * @param what Either IpcMessageBridgeWhatFlags.WHAT_RESPONSE or IpcMessageBridgeWhatFlags.WHAT_FLOW_EMISSION
 * @param data [Bundle] with the response data to be sent back to the client
 */
fun Messenger.sendResponseMessage(
    what: Int,
    messageId: Int,
    data: Bundle,
) {
    send(
        Message.obtain().also {
            it.what = what
            it.arg1 = messageId
            it.data = data
        }
    )
}

fun Messenger.sendResponseErrorMessage(
    what: Int,
    messageId: Int,
    error: Throwable,
) {
    send(
        Message.obtain().also {
            it.what = what
            it.arg1 = messageId
            it.data = Bundle().also { bundle ->
                bundle.putInt(XapiIpcKeys.KEY_STATUS_CODE, error.unwrapHttpStatusCode() ?: 500)
            }
        }
    )
}
