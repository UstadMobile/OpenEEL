package org.openeel.lib.xapi.resources.local

import org.openeel.lib.xapi.resources.XapiActivityProfileResource

interface XapiActivityProfileResourceLocal : XapiActivityProfileResource, XapiDocumentResourceLocal<
    XapiActivityProfileResource.MultiDocParams, XapiActivityProfileResource.SingleDocumentParams
>
