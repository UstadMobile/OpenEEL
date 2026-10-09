package world.respect.xapi.ipc.client

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import world.respect.lib.dataloadstate.DataErrorResult
import world.respect.lib.dataloadstate.DataLoadState
import org.openeel.lib.ipc.messagebridge.MessageData
import org.openeel.lib.ipc.messagebridge.IpcMessageBridge
import world.respect.xapi.ipc.shared.messages.ext.toDataLoadState

suspend fun <T:Any> IpcMessageBridge.executeRequestAsDataLoadState(
    request: MessageData,
    json: Json,
    deserializer: DeserializationStrategy<T>,
): DataLoadState<T> {
    return try {
        executeForResponse(request).data.toDataLoadState(json, deserializer)
    }catch(e: Throwable) {
        DataErrorResult(e)
    }
}