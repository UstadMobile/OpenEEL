package world.respect.lib.xapi.resources

import kotlinx.coroutines.flow.Flow
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.xapi.model.XapiDocument

/**
 * Generic interface for Experience API document resources including the activity profile resource,
 * the agent profile resource, and the state resource.
 *
 *
 * @param MultiDocParams the params class that is used to get a list of available document ids
 * @param SingleDocParams the params class that is used to get, post, put, or delete a single
 *        document.
 */
interface XapiDocumentResource<MultiDocParams: Any, SingleDocParams: ISingleDocumentParams<MultiDocParams>> {

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

    /**
     * Delete a document as per
     *
     * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#single-agent-or-profile-document-put--post--get--delete
     *
     * When a deletion is made this is tracked using the deletions profile which maintains a state
     * document of deleted profiles:
     * {
     *   "deleted-profile-id-1": "2026-01-01T00:00:00Z",
     *   "deleted-profile-id-2": "2026-01-02T00:00:00Z",
     * }
     * Note: as per the JSON specification ( https://www.json.org/json-en.html ), a JsonObject is
     * an unordered collection, so the order of keys is not fixed.
     *
     * This logic will ONLY be executed in the offline repository implementation such that:
     * a) The post to the update the deletions profile id is enqueued to the RemoteWriteQueue
     * b) Duplicate requests are avoided (e.g. if this logic executed in the httpclient
     *    implementation and the database it could lead to duplicate requests etc)
     *
     * The deletions profile itself MUST NOT be deleted. When a deletion happens a POST request is
     * made to the Document Resource with the deletion. Because as per the xAPI spec JSON POST
     * requests are merged, this results in a state document listing the deleted profiles and when
     * they were deleted. This enables proper synchronization of deletions: an offline first
     * repository can determine if a profileId no longer present in the remote was actually deleted
     * (and local data should be deleted to reflect this) or local data has not yet synced to the
     * remote (and local data should not be deleted).
     *
     */
    suspend fun delete(
        params: SingleDocParams
    )

    companion object {

        const val PROFILE_ID_DELETIONS = "https://id.openeel.org/xapi/profile/deletions"

    }
}