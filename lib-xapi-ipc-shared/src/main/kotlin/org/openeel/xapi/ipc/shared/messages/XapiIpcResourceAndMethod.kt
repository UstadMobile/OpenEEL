package world.respect.xapi.ipc.shared.messages

import world.respect.xapi.ipc.shared.messages.XapiIpcResourceFlags.FLAG_TO_ENUMS_MAP

/**
 * Simple data pair class that includes the resource and method for a given request. This is
 * sent in the [android.os.Message.arg2] field of the request message.
 */
data class XapiIpcResourceAndMethod(
    val resource: XapiIpcResourceEnum,
    val method: XapiIpcMethodEnum,
) {

    /**
     * Convert this XapiIpcResourceAndOperation into an Integer that can be used as the value for
     * [android.os.Message.arg2]
     *
     */
    fun toArg2Int(): Int {
        return XapiIpcResourceFlags.ENUMS_TO_FLAG_MAP[this]
            ?: throw IllegalArgumentException("No XapiIpcResourceFlags value for $this")
    }

    companion object {

        fun fromArg2Int(value: Int): XapiIpcResourceAndMethod {
            return FLAG_TO_ENUMS_MAP[value]
                ?: throw IllegalArgumentException("No XapiIpcResourceAndOperation for $value")
        }
    }
}