package world.respect.xapi.ipc.shared.messages

object XapiIpcKeys {

    const val KEY_BODY = "body"

    const val KEY_HEADERS = "headers"

    const val KEY_STATUS_CODE = "status"

    const val KEY_QUERY_PARAMS = "queryParams"

    const val KEY_ENDPOINT = "endpoint"

    const val KEY_AUTH = "auth"

    /**
     * Key used by the client when a) sending an intent to bind to the service and b) sending a
     * message. This is the package name of the client app. It can be spoofed and is used only for
     * debug/logging purposes (similar to the user-agent header on http).
     */
    const val KEY_CLIENT_PACKAGE = "xapiIpcClientPackage"

    /**
     * Key used to store the file descriptor which holds the body of a
     * [world.respect.lib.xapi.model.XapiDocument] as per
     * [world.respect.xapi.ipc.shared.messages.ext.toBundle]
     */
    const val KEY_XAPI_DOC_FD = "xapiDocFd"

    const val KEY_XAPI_DOC_TYPE = "xapiDocType"

    const val KEY_XAPI_DOC_UPDATED = "xapiDocModified"

}