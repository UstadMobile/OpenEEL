package world.respect.lib.xapi.nanohttpd.resources

import fi.iki.elonen.NanoHTTPD
import kotlinx.serialization.json.Json
import world.respect.lib.xapi.XapiResourceProvider
import world.respect.lib.xapi.nanohttpd.ext.parametersAsKtorParams
import world.respect.lib.xapi.resources.XapiActivityProfileResource
import world.respect.lib.xapi.resources.XapiResource

class XapiActivityProfileResourceResponder(
    resourceProvider: XapiResourceProvider,
    json: Json,
) : AbstractXapiDocumentResourceResponder<
    XapiActivityProfileResource.MultiDocParams,
    XapiActivityProfileResource.SingleDocumentParams,
    XapiActivityProfileResource
>(resourceProvider, json) {

    override fun XapiResource.documentResource(): XapiActivityProfileResource = this.activityProfile

    override fun NanoHTTPD.IHTTPSession.isMultiDocRequest(): Boolean {
        return parameters["profileId"].isNullOrEmpty()
    }

    override fun NanoHTTPD.IHTTPSession.getMultiDocParams(): XapiActivityProfileResource.MultiDocParams {
        return XapiActivityProfileResource.MultiDocParams.fromParameters(
            params = parametersAsKtorParams()
        )
    }

    override fun NanoHTTPD.IHTTPSession.getSingleDocParams(): XapiActivityProfileResource.SingleDocumentParams {
        return XapiActivityProfileResource.SingleDocumentParams.fromParameters(
            params = parametersAsKtorParams()
        )
    }
}
