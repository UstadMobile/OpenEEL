package org.openeel.libxapi.test

import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.util.sha1
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.lib.dataloadstate.datetime.toInstant
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.xapi.exceptions.XapiException
import org.openeel.lib.xapi.ext.decodeFromXapiDocument
import org.openeel.lib.xapi.ext.getJson
import org.openeel.lib.xapi.ext.mergeTopLevel
import org.openeel.lib.xapi.ext.postJson
import org.openeel.lib.xapi.ext.putJson
import org.openeel.lib.xapi.model.XapiDocument
import org.openeel.lib.xapi.resources.ISingleDocumentParams
import org.openeel.lib.xapi.resources.XapiDocumentResource
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

abstract class AbstractXapiDocumentResourceTest<
        MultiDocParams: Any,
        SingleDocParams: ISingleDocumentParams<MultiDocParams>,
        T: XapiDocumentResource<MultiDocParams, SingleDocParams>
> {

    private val json = Json

    abstract suspend fun withXapiDocumentResource(
        block: suspend (T) -> Unit
    )

    /**
     * Asserts that an document can be stored via [XapiDocumentResource.put] and subsequently
     * retrieved via [XapiDocumentResource.get].
     *
     * Verifies that:
     * - Storing the document via `put` completes successfully.
     * - Retrieving via `get` returns a [DataReadyState] containing the document.
     * - The retrieved document matches the stored document's content type, content bytes, and updated timestamp.
     *
     */
    fun givenDocument_whenPut_thenCanBeRetrieved(
        documentParams: SingleDocParams
    ) = runBlocking {
        withXapiDocumentResource { resource ->
            val document = XapiActivityProfileTestParams.DOC

            resource.put(documentParams, document)

            val loadState = resource.get(documentParams)
            assertIs<DataReadyState<XapiDocument>>(loadState)

            val retrieved = loadState.data
            assertEquals(document.type, retrieved.type)
            assertContentEquals(
                document.contentsAsByteArray(),
                retrieved.contentsAsByteArray()
            )

            assertEquals(document.updated, retrieved.updated)
            val expectedEtag = sha1(document.contentsAsByteArray()).toHexString()
            assertEquals(expectedEtag, loadState.metaInfo.headers[HttpHeaders.ETag])
        }
    }

    abstract fun givenDocument_whenPut_thenCanBeRetrieved()

    /**
     * Check if the resource supports validation using the If-Modified-Since header parameter.
     *
     * Important: The Last-Modified timestamp on the resource will be the time the resource actually
     * stored it. When a mobile client stores a document, it's last-modified time will be when the
     * mobile client stored it. The last-modified time on the server will be when the server stored
     * it.
     */
    fun givenDocumentStoredAndNotModified_whenRetrievedWithValidationHeaders_thenReturnsNotModified(
        documentParams: SingleDocParams,
    ) = runBlocking {
        withXapiDocumentResource { resource ->
            val document = XapiActivityProfileTestParams.DOC
            resource.put(
                params = documentParams,
                document = document
            )

            val initResponse = resource.get(
                params = documentParams,
            )

            val initResponseLastMod = initResponse.metaInfo.headers[HttpHeaders.LastModified]
            assertNotNull(initResponseLastMod)

            val initResponseETag = initResponse.metaInfo.headers[HttpHeaders.ETag]
            assertNotNull(initResponseETag)

            val response = resource.get(
                params = documentParams,
                dataLoadParams = DataLoadParams(
                    requestHeaders = headersOf(
                        HttpHeaders.IfModifiedSince to listOf(initResponseLastMod)
                    )
                )
            )

            assertIs<NoDataLoadedState<XapiDocument>>(response)
            assertEquals(NoDataLoadedState.Reason.NOT_MODIFIED, response.reason)

            val responseWithIfNoneMatch = resource.get(
                params = documentParams,
                dataLoadParams = DataLoadParams(
                    requestHeaders = headersOf(
                        HttpHeaders.IfNoneMatch to listOf(initResponseETag)
                    )
                )
            )

            assertIs<NoDataLoadedState<XapiDocument>>(responseWithIfNoneMatch)
            assertEquals(NoDataLoadedState.Reason.NOT_MODIFIED, responseWithIfNoneMatch.reason)
        }
    }

    abstract fun givenDocumentStoredAndNotModified_whenRetrievedWithValidationHeaders_thenReturnsNotModified()

    /**
     * Asserts that attempting to retrieve a non-existent activity profile document returns [NoDataLoadedState].
     */
    fun givenNonExistentDocument_whenGetCalled_thenReturnsNotFound(
        nonExistentParams: SingleDocParams,
    ) = runBlocking {
        withXapiDocumentResource { resource ->
            val loadState = resource.get(
                params = nonExistentParams
            )
            assertIs<NoDataLoadedState<XapiDocument>>(loadState)
            assertNull(loadState.dataOrNull())
        }
    }

    abstract fun givenNonExistentDocument_whenGetCalled_thenReturnsNotFound()

    /**
     * Asserts that overwriting an existing activity profile document with [XapiDocumentResource.put]
     * replaces the entire document rather than merging properties.
     */
    fun givenExistingDocument_whenOverwrittenWithPut_thenReplacesDocumentCompletely(
        documentParams: SingleDocParams,
    ) = runBlocking {
        withXapiDocumentResource { resource ->
            val initialDocument = XapiActivityProfileTestParams.DOC_JSON
            val overwritingDocument = XapiActivityProfileTestParams.DOC_UPDATED_JSON

            resource.putJson(documentParams, initialDocument, json, JsonObject.serializer())
            resource.putJson(documentParams, overwritingDocument, json, JsonObject.serializer())

            val retrievedJson = resource.getJson(documentParams, json, JsonObject.serializer())
            assertIs<DataReadyState<JsonObject>>(retrievedJson)
            val retrievedJsonObj = retrievedJson.data

            initialDocument.keys.filter { key ->
                key !in overwritingDocument.keys
            }.forEach { key ->
                assertNull(retrievedJsonObj[key])
            }

            assertEquals(
                expected = overwritingDocument,
                actual = retrievedJsonObj,
            )
        }
    }

    abstract fun givenExistingDocument_whenOverwrittenWithPut_thenReplacesDocumentCompletely()

    /**
     * Asserts that posting to a non-existent activity profile document creates a new document.
     */
    fun givenNonExistentDocument_whenPosted_thenCreatesNewDocument(
        documentParams: SingleDocParams,
    ) = runBlocking {
        withXapiDocumentResource { resource ->
            val document = XapiActivityProfileTestParams.DOC

            resource.post(documentParams, document)

            val loadState = resource.get(documentParams)
            val retrieved = loadState.dataOrNull()
            assertNotNull(retrieved)
            assertContentEquals(
                document.contentsAsByteArray(),
                retrieved.contentsAsByteArray()
            )
            val expectedEtag = sha1(document.contentsAsByteArray()).toHexString()
            assertEquals(expectedEtag, loadState.metaInfo.headers[HttpHeaders.ETag])
        }
    }

    abstract fun givenNonExistentDocument_whenPosted_thenCreatesNewDocument()

    /**
     * Asserts that posting a JSON document to an existing JSON activity profile document
     * merges top-level properties according to the xAPI specification.
     */
    fun givenExistingJsonDocument_whenPosted_thenMergesTopLevelProperties(
        documentParams: SingleDocParams,
    ) = runBlocking {
        withXapiDocumentResource { resource ->
            val initJsonObj = buildJsonObject {
                put("propA", JsonPrimitive("oldA"))
                put("propB", JsonPrimitive("keepB"))
                put(
                    "nested",
                    JsonObject(mapOf("sub1" to JsonPrimitive(100)))
                )
            }

            val updatedPosted = buildJsonObject {
                put("propA", JsonPrimitive("newA"))
                put("propC", JsonPrimitive("addedC"))
                put(
                    key = "nested",
                    element = JsonObject(mapOf("sub2" to JsonPrimitive(200)))
                )
            }

            resource.putJson(
                docParams = documentParams,
                document = initJsonObj,
                json = json,
                serializer = JsonObject.serializer(),
            )

            resource.postJson(
                docParams = documentParams,
                document = updatedPosted,
                json = json,
                serializer = JsonObject.serializer(),
            )

            val getResult = resource.get(params = documentParams)
            val document = getResult.dataOrNull()
            assertNotNull(document)

            val jsonObjRetrieved = json.decodeFromXapiDocument(
                deserializer = JsonObject.serializer(),
                document = document,
            )

            assertEquals(
                expected = initJsonObj.mergeTopLevel(updatedPosted),
                actual = jsonObjRetrieved,
            )

            assertEquals(
                expected = sha1(document.contentsAsByteArray()).toHexString(),
                actual = getResult.metaInfo.headers[HttpHeaders.ETag]
            )
        }
    }

    abstract fun givenExistingJsonDocument_whenPosted_thenMergesTopLevelProperties()

    /**
     * Asserts that posting a non-JSON document to an existing activity profile document
     * throws a [XapiException] with HTTP 400 Bad Request status.
     */
    fun givenNonJsonDocument_whenPostedToExisting_thenThrowsXapiException(
        documentParams: SingleDocParams,
    ) = runBlocking {
        withXapiDocumentResource { resource ->
            val document = XapiActivityProfileTestParams.DOC_NON_JSON

            resource.put(documentParams, document)

            val exception = assertFailsWith<XapiException> {
                resource.post(documentParams, document)
            }
            assertEquals(400, exception.httpStatusCode)
        }
    }

    abstract fun givenNonJsonDocument_whenPostedToExisting_thenThrowsXapiException()

    /**
     * Asserts that an activity profile document can be deleted via [XapiDocumentResource.delete]
     * and that subsequent calls to [XapiDocumentResource.get] return [NoDataLoadedState].
     */
    fun givenDocument_whenDeleted_thenCannotBeRetrieved(
        documentParams: SingleDocParams,
    ) = runBlocking {
        withXapiDocumentResource { resource ->
            val document = XapiActivityProfileTestParams.DOC

            resource.put(documentParams, document)
            assertNotNull(resource.get(documentParams).dataOrNull())

            resource.delete(documentParams)

            val loadState = resource.get(documentParams)
            assertIs<NoDataLoadedState<XapiDocument>>(loadState)
            assertNull(loadState.dataOrNull())
        }
    }

    abstract fun givenDocument_whenDeleted_thenCannotBeRetrieved()

    /**
     * Asserts that [XapiDocumentResource.getMultipleDocuments] returns all document IDs
     * associated with a given [MultiDocParams].
     */
    fun givenMultipleDocuments_whenGetMultipleDocumentsCalled_thenReturnsAllProfileIdsForActivity(
        documentsAndParams: List<Pair<XapiDocument, SingleDocParams>>,
        indexToSearch: Int = 0,
    ) = runBlocking {
        withXapiDocumentResource { resource ->
            documentsAndParams.forEach {
                resource.put(it.second, it.first)
            }

            val paramsToSearchFor = documentsAndParams[indexToSearch].second
            val multiDocSearchParams = paramsToSearchFor.toMultiDocParams()

            val idStrings = resource.getMultipleDocuments(
                params = multiDocSearchParams
            ).dataOrNull()
            assertNotNull(idStrings)
            assertEquals(
                expected = documentsAndParams.filter {
                    it.second.matches(multiDocSearchParams)
                }.map { it.second.idString },
                actual = idStrings
            )
        }
    }

    abstract fun givenMultipleDocuments_whenGetMultipleDocumentsCalled_thenReturnsAllProfileIdsForActivity()

    /**
     * Asserts that [XapiDocumentResource.getMultipleDocuments] with a `since` parameter
     * returns only document IDs for documents updated after the given timestamp.
     */
    fun givenMultipleDocumentsWithTimestamps_whenGetMultipleDocumentsWithSince_thenReturnsOnlyNewerProfileIds(
        documentsAndParams: List<Pair<XapiDocument, SingleDocParams>>,
    ) = runBlocking {
        withXapiDocumentResource { resource ->
            documentsAndParams.forEach { (doc, params) ->
                resource.put(params, doc)
            }

            documentsAndParams.forEach { (doc, docParams) ->
                val since = doc.updated.toInstant()
                val multiDocParams = docParams.toMultiDocParams(since = since)

                assertEquals(
                    expected = documentsAndParams.filter {
                        it.second.matches(multiDocParams) && it.first.updated.toInstant() > since
                    }.map {
                        it.second.idString
                    }.toSet(),
                    actual = resource.getMultipleDocuments(
                        params = multiDocParams
                    ).dataOrNull()?.toSet()
                )
            }
        }
    }

    abstract fun givenMultipleDocumentsWithTimestamps_whenGetMultipleDocumentsWithSince_thenReturnsOnlyNewerProfileIds()

}