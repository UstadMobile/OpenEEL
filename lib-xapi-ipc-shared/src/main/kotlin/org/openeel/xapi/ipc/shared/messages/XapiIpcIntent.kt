package world.respect.xapi.ipc.shared.messages

object XapiIpcIntent {

    /**
     * Used by other apps to connect to Xapi IPC service
     */
    @Suppress("unused")
    const val ACTION_XAPI_OVER_IPC = "org.openeel.action.xapioveripc"

    /**
     * The package name of the app that provides the xAPI IPC service. This is added sa a parameter
     * to the launch URL used to launch the client app so the client app can bind (explicitly) to
     * the xAPI IPC service.
     */
    const val PARAM_NAME_IPC_SERVICE_PACKAGE = "xapiIpcPackage"

}