package world.respect.lib.xapi.nanohttpd.resources

import fi.iki.elonen.NanoHTTPD
import kotlinx.serialization.json.Json
import world.respect.lib.xapi.XapiResourceProvider
import world.respect.lib.xapi.nanohttpd.ext.parametersAsKtorParams
import world.respect.lib.xapi.resources.XapiResource
import world.respect.lib.xapi.resources.XapiStateResource

class XapiStateResourceResponder(
    resourceProvider: XapiResourceProvider,
    private val json: Json,
) : AbstractXapiDocumentResourceResponder<
    XapiStateResource.MultiDocParams,
    XapiStateResource.SingleDocumentParams,
    XapiStateResource
>(resourceProvider, json) {

    override fun XapiResource.documentResource(): XapiStateResource = this.state

    override fun NanoHTTPD.IHTTPSession.isMultiDocRequest(): Boolean {
        return parameters["stateId"].isNullOrEmpty()
    }

    override fun NanoHTTPD.IHTTPSession.getMultiDocParams(): XapiStateResource.MultiDocParams {
        return XapiStateResource.MultiDocParams.fromParameters(
            params = parametersAsKtorParams(),
            json = json
        )
    }

    override fun NanoHTTPD.IHTTPSession.getSingleDocParams(): XapiStateResource.SingleDocumentParams {
        return XapiStateResource.SingleDocumentParams.fromParameters(
            params = parametersAsKtorParams(),
            json = json
        )
    }
}

