package org.openeel.lib.xapi.resources

import io.ktor.http.Parameters
import kotlinx.serialization.json.Json
import kotlin.time.Instant

interface XapiDocumentResourceParamsAdapter<
        MultiDocParams: Any, SingleDocParams: ISingleDocumentParams
> {

    /**
     * Each of the xAPI document resources (Activity Profile, State, and Agent profile)
     * MultiDocParams require all the parameters of the SingleDocParams except string id (
     * profileId or stateId as per [idString].
     *
     * All MultiDocParams always include the since parameter.
     *
     * @param since the since parameter to use for the MutliDocParams to be returned
     * @return MultiDocParams corresponding to these SingleDocumentParams
     */
    fun singleDocParamsToMutliDocParams(
        params: SingleDocParams,
        since: Instant? = null,
    ): MultiDocParams


    fun singleDocParamsMatchMultiDocParams(
        singleDocParams: SingleDocParams,
        multiDocParams: MultiDocParams,
    ): Boolean

    fun singleDocParamsFromParameters(
        params: Parameters,
        json: Json,
    ): SingleDocParams

}