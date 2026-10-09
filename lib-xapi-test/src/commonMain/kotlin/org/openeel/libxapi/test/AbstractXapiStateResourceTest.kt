package org.openeel.libxapi.test

import kotlinx.coroutines.runBlocking
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.lib.dataloadstate.datetime.toGMTDate
import org.openeel.lib.xapi.exceptions.XapiException
import org.openeel.lib.xapi.model.XapiDocumentByteArrayImpl
import org.openeel.lib.xapi.resources.XapiStateResource
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

abstract class AbstractXapiStateResourceTest : AbstractXapiDocumentResourceTest<XapiStateResource.MultiDocParams, XapiStateResource.SingleDocumentParams, XapiStateResource>() {

    @Test
    override fun givenDocument_whenPut_thenCanBeRetrieved() = runBlocking {
        givenDocument_whenPut_thenCanBeRetrieved(
            documentParams = XapiStateTestParams.SINGLE_DOC_PARAMS1
        )
    }

    /**
     * Check if the resource supports validation using the If-Modified-Since header parameter.
     *
     * Important: The Last-Modified timestamp on the resource will be the time the resource actually
     * stored it. When a mobile client stores a document, it's last-modified time will be when the
     * mobile client stored it. The last-modified time on the server will be when the server stored
     * it.
     */
    @Test
    override fun givenDocumentStoredAndNotModified_whenRetrievedWithValidationHeaders_thenReturnsNotModified() = runBlocking {
        givenDocumentStoredAndNotModified_whenRetrievedWithValidationHeaders_thenReturnsNotModified(
            documentParams = XapiStateTestParams.SINGLE_DOC_PARAMS1,
        )
    }

    /**
     * Asserts that attempting to retrieve a non-existent state document returns [NoDataLoadedState].
     */
    @Test
    override fun givenNonExistentDocument_whenGetCalled_thenReturnsNotFound() = runBlocking {
        givenNonExistentDocument_whenGetCalled_thenReturnsNotFound(
            nonExistentParams = XapiStateTestParams.SINGLE_DOC_NON_EXISTENT_PARAMS
        )
    }

    /**
     * Asserts that overwriting an existing state document with [XapiStateResource.put]
     * replaces the entire document rather than merging properties.
     */
    @Test
    override fun givenExistingDocument_whenOverwrittenWithPut_thenReplacesDocumentCompletely() = runBlocking {
        givenExistingDocument_whenOverwrittenWithPut_thenReplacesDocumentCompletely(
            documentParams = XapiStateTestParams.SINGLE_DOC_PARAMS1
        )
    }

    /**
     * Asserts that posting to a non-existent state document creates a new document.
     */
    @Test
    override fun givenNonExistentDocument_whenPosted_thenCreatesNewDocument() = runBlocking {
        givenNonExistentDocument_whenPosted_thenCreatesNewDocument(
            documentParams = XapiStateTestParams.SINGLE_DOC_PARAMS1
        )
    }

    /**
     * Asserts that posting a JSON document to an existing JSON state document
     * merges top-level properties according to the xAPI specification.
     */
    @Test
    override fun givenExistingJsonDocument_whenPosted_thenMergesTopLevelProperties() = runBlocking {
        givenExistingJsonDocument_whenPosted_thenMergesTopLevelProperties(
            documentParams = XapiStateTestParams.SINGLE_DOC_PARAMS1
        )
    }

    /**
     * Asserts that posting a non-JSON document to an existing state document
     * throws a [XapiException] with HTTP 400 Bad Request status.
     */
    @Test
    override fun givenNonJsonDocument_whenPostedToExisting_thenThrowsXapiException() = runBlocking {
        givenNonJsonDocument_whenPostedToExisting_thenThrowsXapiException(
            documentParams = XapiStateTestParams.SINGLE_DOC_PARAMS1
        )
    }

    /**
     * Asserts that a state document can be deleted via [XapiStateResource.delete]
     * and that subsequent calls to [XapiStateResource.get] return [NoDataLoadedState].
     */
    @Test
    override fun givenDocument_whenDeleted_thenCannotBeRetrieved() = runBlocking {
        givenDocument_whenDeleted_thenCannotBeRetrieved(
            documentParams = XapiStateTestParams.SINGLE_DOC_PARAMS1
        )
    }

    /**
     * Asserts that [XapiStateResource.getMultipleDocuments] returns all state IDs
     * associated with a given activity ID, agent, and registration.
     */
    @Test
    override fun givenMultipleDocuments_whenGetMultipleDocumentsCalled_thenReturnsAllProfileIdsForActivity() = runBlocking {
        givenMultipleDocuments_whenGetMultipleDocumentsCalled_thenReturnsAllProfileIdsForActivity(
            documentsAndParams = listOf(
                Pair(XapiStateTestParams.ACTIVITY_ID1, XapiStateTestParams.AGENT1),
                Pair(XapiStateTestParams.ACTIVITY_ID2, XapiStateTestParams.AGENT2),
            ).flatMapIndexed { docIndex, (activityId, agent) ->
                (1..2).map { paramNum ->
                    Pair(
                        first = XapiDocumentByteArrayImpl(
                            type = "application/json",
                            updated = Clock.System.now().toGMTDate(),
                            contents = """{"p": $paramNum}""".encodeToByteArray(),
                        ),
                        second = XapiStateResource.SingleDocumentParams(
                            activityId = activityId,
                            agent = agent,
                            registration = XapiStateTestParams.REGISTRATION1,
                            stateId = "s$docIndex-$paramNum",
                        ),
                    )
                }
            }
        )
    }

    /**
     * Asserts that [XapiStateResource.getMultipleDocuments] with a `since` parameter
     * returns only state IDs for documents updated after the given timestamp.
     */
    @Test
    override fun givenMultipleDocumentsWithTimestamps_whenGetMultipleDocumentsWithSince_thenReturnsOnlyNewerProfileIds() = runBlocking {
        val baseTime = Instant.parse("2026-01-01T10:00:00Z")

        givenMultipleDocumentsWithTimestamps_whenGetMultipleDocumentsWithSince_thenReturnsOnlyNewerProfileIds(
            documentsAndParams = listOf(
                Pair(XapiStateTestParams.ACTIVITY_ID1, XapiStateTestParams.AGENT1),
                Pair(XapiStateTestParams.ACTIVITY_ID2, XapiStateTestParams.AGENT2),
            ).flatMapIndexed { docIndex, (activityId, agent) ->
                (1..3).map { paramNum ->
                    val updated = baseTime + (paramNum * 10).minutes
                    Pair(
                        first = XapiDocumentByteArrayImpl(
                            type = "application/json",
                            updated = updated.toGMTDate(),
                            contents = """{"p": $paramNum}""".encodeToByteArray(),
                        ),
                        second = XapiStateResource.SingleDocumentParams(
                            activityId = activityId,
                            agent = agent,
                            registration = XapiStateTestParams.REGISTRATION1,
                            stateId = "s$docIndex-$paramNum",
                        ),
                    )
                }
            },
        )
    }
}
