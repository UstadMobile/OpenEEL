package org.openeel.lib.xapi.nanohttpd.resources

import fi.iki.elonen.NanoHTTPD
import kotlinx.serialization.json.Json
import org.openeel.lib.xapi.XapiResourceProvider
import org.openeel.lib.xapi.nanohttpd.ext.parametersAsKtorParams
import org.openeel.lib.xapi.resources.XapiActivityProfileResource
import org.openeel.lib.xapi.resources.XapiResource

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
