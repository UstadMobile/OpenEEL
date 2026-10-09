package world.respect.xapi.ipc.client

import android.content.Context
import android.content.Intent
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import org.openeel.lib.ipc.messagebridge.IpcMessageBridgeServiceConnectionImpl
import world.respect.lib.xapi.resources.XapiResource
import world.respect.xapi.ipc.shared.messages.XapiIpcIntent
import world.respect.xapi.ipc.shared.messages.XapiIpcKeys

class XapiIpcClientBuilder(
    private val context: Context,
    private val endpointUrl: String,
) {

    private var json: Json? = null

    private var ipcPackageServiceName: String? = null

    private var auth: String? = null

    fun setJson(json : Json) : XapiIpcClientBuilder{
        this.json = json
        return this
    }

    fun setIpcServicePackageName(
        ipcPackageServiceName: String
    ): XapiIpcClientBuilder {
        this.ipcPackageServiceName = ipcPackageServiceName
        return this
    }

    fun setAuth(auth: String): XapiIpcClientBuilder {
        this.auth = auth
        return this
    }


    fun build(): XapiResource {
        return XapiResourceIpcClient(
            requestSender = IpcMessageBridgeServiceConnectionImpl(
                context = context,
                intent = Intent(XapiIpcIntent.ACTION_XAPI_OVER_IPC).also {
                    it.`package` = ipcPackageServiceName
                        ?: throw IllegalArgumentException("Ipc service package not set")
                    it.putExtra(XapiIpcKeys.KEY_CLIENT_PACKAGE, context.packageName)
                }
            ),
            json = json ?: Json { encodeDefaults = false },
            endpoint = Url(endpointUrl),
            auth = auth ?: throw IllegalArgumentException("No auth provided"),
            clientPackageName = context.packageName,
        )
    }

}