package world.respect.lib.xapi.nanohttpd.resources

import fi.iki.elonen.NanoHTTPD
import kotlinx.serialization.json.Json
import world.respect.lib.xapi.XapiResourceProvider
import world.respect.lib.xapi.nanohttpd.ext.parametersAsKtorParams
import world.respect.lib.xapi.resources.XapiAgentProfileResource
import world.respect.lib.xapi.resources.XapiResource

class XapiAgentProfileResourceResponder(
    resourceProvider: XapiResourceProvider,
    private val json: Json,
) : AbstractXapiDocumentResourceResponder<
    XapiAgentProfileResource.MultiDocParams,
    XapiAgentProfileResource.SingleDocumentParams,
    XapiAgentProfileResource
>(resourceProvider, json) {

    override fun XapiResource.documentResource(): XapiAgentProfileResource = this.agentProfile

    override fun NanoHTTPD.IHTTPSession.isMultiDocRequest(): Boolean {
        return parameters["profileId"].isNullOrEmpty()
    }

    override fun NanoHTTPD.IHTTPSession.getMultiDocParams(): XapiAgentProfileResource.MultiDocParams {
        return XapiAgentProfileResource.MultiDocParams.fromParameters(
            params = parametersAsKtorParams(),
            json = json
        )
    }

    override fun NanoHTTPD.IHTTPSession.getSingleDocParams(): XapiAgentProfileResource.SingleDocumentParams {
        return XapiAgentProfileResource.SingleDocumentParams.fromParameters(
            params = parametersAsKtorParams(),
            json = json
        )
    }
}

typealias AgentProfileResourceResponder = XapiAgentProfileResourceResponder
