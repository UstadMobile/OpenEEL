package org.openeel.lib.xapi.resources

import kotlinx.coroutines.flow.Flow
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.xapi.model.XapiDocument

/**
 * Generic interface for Experience API document resources including the activity profile resource,
 * the agent profile resource, and the state resource.
 *
 *
 * @param MultiDocParams the params class that is used to get a list of available document ids
 * @param SingleDocParams the params class that is used to get, post, put, or delete a single
 *        document.
 */
interface XapiDocumentResource<MultiDocParams: Any, SingleDocParams: ISingleDocumentParams> {

    /**
     * Get a list of available document ids e.g. stateIds, profileIds etc.
     */
    suspend fun getMultipleDocuments(
        params: MultiDocParams,
        dataLoadParams: DataLoadParams = DataLoadParams()
    ): DataLoadState<List<String>>


    /**
     * Get a single document.
     *
     * @param params the SingleDocParams that identify the document for this type of
     *        resource e.g. Activity Profile API, State API, etc.
     * @param dataLoadParams DataLoadParams for the request. The If-Not-Modified-Since and
     *        If-None-Match headers will be checked and if the document has not been modified
     *        then NoDataLoadedState.notModified() will be returned.
     *
     * @return the Document as a [DataLoadState].
     *         If no document exists, then NoDataLoadedState.notFound()
     *         If the document has not been modified as per If-Not-Modified-Since or If-None-Match
     *         headers, then NoDataLoadedState.notModified()
     *         Otherwise, DataReadyState with the document data.
     *
     *         The DataLoadMetaInfo of the returned DataLoadState will include:
     *          a) The Last-Modified header
     *          b) The ETag header, which will be the SHA-1 digest as a hexadecimal string of the
     *             document contents as per:
     *             https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#part-three-data-processing-validation-and-security
     */
    suspend fun get(
        params: SingleDocParams,
        dataLoadParams: DataLoadParams = DataLoadParams(),
    ): DataLoadState<XapiDocument>

    /**
     * Get a single document as a flow. The same as [get] but returns a flow of [DataLoadState]
     *
     * @param params the SingleDocParams that identify the document for this type of
     *        resource e.g. Activity Profile API, State API, etc.
     * @param dataLoadParams DataLoadParams for the request. The If-Not-Modified-Since and
     *        If-None-Match headers will be checked and if the document has not been modified
     *        then NoDataLoadedState.notModified() will be returned.
     *
     * @return flow of the Document as a [DataLoadState].
     */
    fun getAsFlow(
        params: SingleDocParams,
        dataLoadParams: DataLoadParams = DataLoadParams(),
    ): Flow<DataLoadState<XapiDocument>>

    /**
     * Post (e.g. update/insert) an xAPI activity profile document as per
     *
     * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#single-agent-or-profile-document-put--post--get--delete
     *
     * As per the xAPI spec: because this is a POST request, if the content-type is application/json
     * it MUST merge the posted document with the existing document. Only top level properties are
     * merged. The entire contents of each original property are replaced with the entire contents
     * of each new property.
     *
     * ONLY application/JSON may be posted.
     */
    suspend fun post(
        params: SingleDocParams,
        document: XapiDocument
    )

    /**
     * Put (e.g. update/insert) an xAPI activity profile document as per
     *
     * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#single-agent-or-profile-document-put--post--get--delete
     *
     * As per the xAPI spec: because this is a PUT request, there will be NO merge of the posted
     * document. Any existing document will be overwritten.
     *
     */
    suspend fun put(
        params: SingleDocParams,
        document: XapiDocument
    )


    suspend fun delete(
        params: SingleDocParams
    )

    companion object {

        const val PROFILE_ID_DELETIONS = "https://id.openeel.org/xapi/profile/deletions"

    }
}