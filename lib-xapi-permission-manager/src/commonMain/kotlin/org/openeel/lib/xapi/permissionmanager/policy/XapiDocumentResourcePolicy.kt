package org.openeel.lib.xapi.permissionmanager.policy

import org.openeel.lib.xapi.model.XapiDocument
import org.openeel.lib.xapi.resources.ISingleDocumentParams

/**
 *
 */
interface XapiDocumentResourcePolicy<
    MultiDocParams: Any, SingleDocParams: ISingleDocumentParams<MultiDocParams>
> {

    data class TransformedWrite<T>(
        val document: XapiDocument,
        val params: T,
    )

    suspend fun transformReadSingleDocParams(
        params: SingleDocParams
    ): SingleDocParams

    suspend fun transformReadMultiDocParams(
        params: MultiDocParams
    ): MultiDocParams

    suspend fun transformPostDoc(
        document: XapiDocument,
        params: SingleDocParams
    ): TransformedWrite<SingleDocParams>

    suspend fun transformPutDoc(
        document: XapiDocument,
        params: SingleDocParams
    ): TransformedWrite<SingleDocParams>

    suspend fun transformDeleteDoc(
        params: SingleDocParams
    ): SingleDocParams

}