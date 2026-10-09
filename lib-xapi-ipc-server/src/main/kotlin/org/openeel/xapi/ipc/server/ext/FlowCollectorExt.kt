package world.respect.xapi.ipc.server.ext

import android.os.Message
import android.util.Log
import kotlinx.coroutines.Job
import org.openeel.lib.ipc.messagebridge.IpcMessageBridgeWhatFlags
import world.respect.xapi.ipc.shared.messages.XapiIpcTags

/**
 * Used by xAPI-IPC handlers to handle a flow completion message being received. Cancel the job
 * and remove the given flow from the map.
 */
fun MutableMap<Int, Job>.removeCollectorIfFlowCompleted(message: Message): Boolean {
    if(message.what != IpcMessageBridgeWhatFlags.WHAT_FLOW_COMPLETION)
        return false

    return remove(message.arg1)?.also {
        it.cancel()
        Log.d(XapiIpcTags.LOGTAG, "Flow completed for #${message.arg1}: removed/cancelled")
    } != null
}