package org.openeel.lib.xapi.resources.local

import org.openeel.lib.xapi.resources.XapiStateResource

interface XapiStateResourceLocal : XapiStateResource, XapiDocumentResourceLocal<
        XapiStateResource.MultiDocParams, XapiStateResource.SingleDocumentParams
>
