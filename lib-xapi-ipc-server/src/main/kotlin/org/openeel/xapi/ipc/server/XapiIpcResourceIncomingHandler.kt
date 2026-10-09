package world.respect.xapi.ipc.server

import android.os.Message

/**
 *
 */
interface XapiIpcResourceIncomingHandler {

    fun handleMessage(msg: Message)

}