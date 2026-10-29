package org.openeel.lib.xapi.resources.local

import org.openeel.lib.xapi.model.XapiDocument
import org.openeel.lib.xapi.resources.ISingleDocumentParams
import org.openeel.lib.xapi.resources.XapiDocumentResource

interface XapiDocumentResourceLocal<
        MultiDocParams: Any, SingleDocParams: ISingleDocumentParams
>: XapiDocumentResource<MultiDocParams, SingleDocParams> {

    /**
     * As per other resources: update the given data locally, DO NOT enqueue for writing to the
     * remote endpoint or perform permission checks. This is used by the repository layer.
     */
    suspend fun updateLocal(
        params: SingleDocParams,
        document: XapiDocument,
    )

}