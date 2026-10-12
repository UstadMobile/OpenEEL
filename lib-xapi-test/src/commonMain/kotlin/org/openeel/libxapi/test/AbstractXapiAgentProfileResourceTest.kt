package org.openeel.libxapi.test

import kotlinx.coroutines.runBlocking
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.lib.dataloadstate.datetime.toGMTDate
import org.openeel.lib.xapi.exceptions.XapiException
import org.openeel.lib.xapi.model.XapiDocumentByteArrayImpl
import org.openeel.lib.xapi.resources.XapiAgentProfileResource
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

abstract class AbstractXapiAgentProfileResourceTest: AbstractXapiDocumentResourceTest<XapiAgentProfileResource.MultiDocParams, XapiAgentProfileResource.SingleDocumentParams, XapiAgentProfileResource>() {

    @Test
    override fun givenDocument_whenPut_thenCanBeRetrieved() = runBlocking {
        givenDocument_whenPut_thenCanBeRetrieved(
            documentParams = XapiAgentProfileTestParams.SINGLE_DOC_PARAMS1,
            authenticatedAgents = { listOf(XapiAgentProfileTestParams.SINGLE_DOC_PARAMS1.agent) },
        )
    }

    @Test
    override fun givenDocument_whenPut_thenCanBeRetrievedAsFlow() {
        givenDocument_whenPut_thenCanBeRetrievedAsFlow(
            documentParams = XapiAgentProfileTestParams.SINGLE_DOC_PARAMS1,
            authenticatedAgents = { listOf(XapiAgentProfileTestParams.SINGLE_DOC_PARAMS1.agent) },
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
            documentParams = XapiAgentProfileTestParams.SINGLE_DOC_PARAMS1,
            authenticatedAgents = { listOf(XapiAgentProfileTestParams.SINGLE_DOC_PARAMS1.agent) },
        )
    }

    /**
     * Asserts that attempting to retrieve a non-existent agent profile document returns [NoDataLoadedState].
     */
    @Test
    override fun givenNonExistentDocument_whenGetCalled_thenReturnsNotFound() = runBlocking {
        givenNonExistentDocument_whenGetCalled_thenReturnsNotFound(
            nonExistentParams = XapiAgentProfileTestParams.SINGLE_DOC_NON_EXISTENT_PARAMS,
            authenticatedAgents = { listOf(XapiAgentProfileTestParams.SINGLE_DOC_PARAMS1.agent) },
        )
    }

    /**
     * Asserts that overwriting an existing agent profile document with [XapiAgentProfileResource.put]
     * replaces the entire document rather than merging properties.
     */
    @Test
    override fun givenExistingDocument_whenOverwrittenWithPut_thenReplacesDocumentCompletely() = runBlocking {
        givenExistingDocument_whenOverwrittenWithPut_thenReplacesDocumentCompletely(
            documentParams = XapiAgentProfileTestParams.SINGLE_DOC_PARAMS1,
            authenticatedAgents = { listOf(XapiAgentProfileTestParams.SINGLE_DOC_PARAMS1.agent) },
        )
    }

    /**
     * Asserts that posting to a non-existent agent profile document creates a new document.
     */
    @Test
    override fun givenNonExistentDocument_whenPosted_thenCreatesNewDocument() = runBlocking {
        givenNonExistentDocument_whenPosted_thenCreatesNewDocument(
            documentParams = XapiAgentProfileTestParams.SINGLE_DOC_PARAMS1,
            authenticatedAgents = { listOf(XapiAgentProfileTestParams.SINGLE_DOC_PARAMS1.agent) },
        )
    }

    /**
     * Asserts that posting a JSON document to an existing JSON agent profile document
     * merges top-level properties according to the xAPI specification.
     */
    @Test
    override fun givenExistingJsonDocument_whenPosted_thenMergesTopLevelProperties() = runBlocking {
        givenExistingJsonDocument_whenPosted_thenMergesTopLevelProperties(
            documentParams = XapiAgentProfileTestParams.SINGLE_DOC_PARAMS1,
            authenticatedAgents = { listOf(XapiAgentProfileTestParams.SINGLE_DOC_PARAMS1.agent) },
        )
    }

    /**
     * Asserts that posting a non-JSON document to an existing agent profile document
     * throws a [XapiException] with HTTP 400 Bad Request status.
     */
    @Test
    override fun givenNonJsonDocument_whenPostedToExisting_thenThrowsXapiException() = runBlocking {
        givenNonJsonDocument_whenPostedToExisting_thenThrowsXapiException(
            documentParams = XapiAgentProfileTestParams.SINGLE_DOC_PARAMS1,
            authenticatedAgents = { listOf(XapiAgentProfileTestParams.SINGLE_DOC_PARAMS1.agent) },
        )
    }

    /**
     * Asserts that an agent profile document can be deleted via [XapiAgentProfileResource.delete]
     * and that subsequent calls to [XapiAgentProfileResource.get] return [NoDataLoadedState].
     */
    @Test
    override fun givenDocument_whenDeleted_thenCannotBeRetrieved() = runBlocking {
        givenDocument_whenDeleted_thenCannotBeRetrieved(
            documentParams = XapiAgentProfileTestParams.SINGLE_DOC_PARAMS1,
            authenticatedAgents = { listOf(XapiAgentProfileTestParams.SINGLE_DOC_PARAMS1.agent) },
        )
    }

    /**
     * Asserts that [XapiAgentProfileResource.getMultipleDocuments] returns all profile IDs
     * associated with a given agent.
     */
    @Test
    override fun givenMultipleDocuments_whenGetMultipleDocumentsCalled_thenReturnsAllProfileIdsForActivity() = runBlocking {
        givenMultipleDocuments_whenGetMultipleDocumentsCalled_thenReturnsAllProfileIdsForActivity(
            documentsAndParams = listOf(
                XapiAgentProfileTestParams.AGENT1,
                XapiAgentProfileTestParams.AGENT2,
            ).flatMapIndexed { docIndex, agent ->
                (1..2).map { paramNum ->
                    Pair(
                        first = XapiDocumentByteArrayImpl(
                            type = "application/json",
                            updated = Clock.System.now().toGMTDate(),
                            contents = """{"p": $paramNum}""".encodeToByteArray(),
                        ),
                        second = XapiAgentProfileResource.SingleDocumentParams(
                            agent = agent,
                            profileId = "p$docIndex-$paramNum",
                        ),
                    )
                }
            },
            authenticatedAgents = {
                listOf(XapiAgentProfileTestParams.AGENT1, XapiAgentProfileTestParams.AGENT2)
            },
        )
    }

    /**
     * Asserts that [XapiAgentProfileResource.getMultipleDocuments] with a `since` parameter
     * returns only profile IDs for documents updated after the given timestamp.
     */
    @Test
    override fun givenMultipleDocumentsWithTimestamps_whenGetMultipleDocumentsWithSince_thenReturnsOnlyNewerProfileIds() = runBlocking {
        val baseTime = Instant.parse("2026-01-01T10:00:00Z")

        givenMultipleDocumentsWithTimestamps_whenGetMultipleDocumentsWithSince_thenReturnsOnlyNewerProfileIds(
            documentsAndParams = listOf(
                XapiAgentProfileTestParams.AGENT1,
                XapiAgentProfileTestParams.AGENT2,
            ).flatMapIndexed { docIndex, agent ->
                (1..3).map { paramNum ->
                    val updated = baseTime + (paramNum * 10).minutes
                    Pair(
                        first = XapiDocumentByteArrayImpl(
                            type = "application/json",
                            updated = updated.toGMTDate(),
                            contents = """{"p": $paramNum}""".encodeToByteArray(),
                        ),
                        second = XapiAgentProfileResource.SingleDocumentParams(
                            agent = agent,
                            profileId = "p$docIndex-$paramNum",
                        ),
                    )
                }
            },
            authenticatedAgents = {
                listOf(XapiAgentProfileTestParams.AGENT1, XapiAgentProfileTestParams.AGENT2)
            },
        )
    }
}
