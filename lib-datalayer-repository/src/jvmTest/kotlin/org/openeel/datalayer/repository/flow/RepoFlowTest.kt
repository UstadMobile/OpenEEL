package org.openeel.datalayer.repository.flow

import app.cash.turbine.test
import io.ktor.http.HttpHeaders
import io.ktor.http.headers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.openeel.lib.dataloadstate.DataLoadMetaInfo
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.NoDataLoadedState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class RepoFlowTest {

    @Test
    fun givenNoLocalDataAvailable_whenInvoked_thenRemoteDataLoadParamsAreUnmodified() = runBlocking {
        val initialParams = DataLoadParams(
            requestHeaders = headers {
                append("X-Custom-Header", "test-value")
            }
        )
        var capturedParams: DataLoadParams? = null
        val localFlow = flowOf<DataLoadState<String>>(
            NoDataLoadedState(reason = NoDataLoadedState.Reason.NOT_FOUND)
        )

        val repoFlow = localFlow.asRepoFlow<String, String>(
            dataLoadParams = initialParams,
            remoteFlow = { params ->
                capturedParams = params
                flowOf(NoDataLoadedState(reason = NoDataLoadedState.Reason.NOT_FOUND))
            },
            onRemoteUpdated = {}
        )

        repoFlow.test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        assertNotNull(capturedParams)
        assertEquals(initialParams, capturedParams)
        assertNull(capturedParams.requestHeaders[HttpHeaders.IfNoneMatch])
        assertNull(capturedParams.requestHeaders[HttpHeaders.IfModifiedSince])
    }

    @Test
    fun givenLocalDataAvailableWithValidationInfo_whenInvoked_thenRemoteDataLoadParamsIncludeEtags() = runBlocking {
        val initialParams = DataLoadParams()
        val localHeaders = headers {
            append(HttpHeaders.ETag, "\"123456\"")
            append(HttpHeaders.LastModified, "Sun, 06 Sep 2026 08:49:37 GMT")
        }
        val localFlow = flowOf<DataLoadState<String>>(
            DataReadyState(
                data = "local data",
                metaInfo = DataLoadMetaInfo(headers = localHeaders)
            )
        )
        var capturedParams: DataLoadParams? = null

        val repoFlow = localFlow.asRepoFlow<String, String>(
            dataLoadParams = initialParams,
            remoteFlow = { params ->
                capturedParams = params
                flowOf(NoDataLoadedState(reason = NoDataLoadedState.Reason.NOT_MODIFIED))
            },
            onRemoteUpdated = {}
        )

        repoFlow.test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        assertNotNull(capturedParams)
        assertEquals(
            expected = localHeaders[HttpHeaders.LastModified],
            actual = capturedParams.requestHeaders[HttpHeaders.IfModifiedSince]
        )
        assertEquals(
            expected = localHeaders[HttpHeaders.ETag],
            actual = capturedParams.requestHeaders[HttpHeaders.IfNoneMatch]
        )
    }

    @Test
    fun givenLocalDataAvailable_whenRemoteDataIsUnavailable_thenLocalDataIsEmitted() = runBlocking {
        val localFlow = flowOf<DataLoadState<String>>(
            DataReadyState(data = "cached-data")
        )

        val repoFlow = localFlow.asRepoFlow<String, String>(
            dataLoadParams = DataLoadParams(),
            remoteFlow = {
                flowOf(NoDataLoadedState(reason = NoDataLoadedState.Reason.NOT_FOUND))
            },
            onRemoteUpdated = {}
        )

        repoFlow.filter {
            it.localState is DataReadyState && it.remoteState is NoDataLoadedState
        }.test {
            val item = awaitItem()
            assertTrue(item is DataReadyState)
            assertEquals("cached-data", item.data)
            assertEquals(NoDataLoadedState.Reason.NOT_FOUND, (item.remoteState as? NoDataLoadedState)?.reason)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun givenDuplicateLocalDataEmissions_whenInvoked_thenRemoteFlowIsInvokedOnlyOnce() = runBlocking {
        val localFlow = flow {
            (1..3).forEach {
                emit(
                    DataReadyState(
                        "data-1",
                        metaInfo = DataLoadMetaInfo(
                            headers = headers {
                                append(HttpHeaders.ETag, "\"etag-1\"")
                            }
                        )
                    )
                )
            }
        }
        var remoteInvocationCount = 0

        val repoFlow = localFlow.asRepoFlow<String, String>(
            dataLoadParams = DataLoadParams(),
            remoteFlow = {
                remoteInvocationCount++
                flowOf(NoDataLoadedState(reason = NoDataLoadedState.Reason.NOT_MODIFIED))
            },
            onRemoteUpdated = {}
        )

        repoFlow.test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(1, remoteInvocationCount)
    }

    @Test
    fun givenRemoteDataIsOlder_whenInvoked_thenLocalDataIsEmittedAndOnRemoteUpdatedIsNotCalled() = runBlocking {
        val localHeaders = headers {
            append(HttpHeaders.ETag, "\"local-etag\"")
            append(HttpHeaders.LastModified, "Sun, 06 Sep 2026 08:49:37 GMT")
        }

        val remoteHeaders = headers {
            append(HttpHeaders.ETag, "\"remote-etag\"")
            append(HttpHeaders.LastModified, "Sun, 06 Sep 2026 07:49:37 GMT")
        }

        val localFlow = flowOf<DataLoadState<String>>(
            DataReadyState(
                data = "local-data",
                metaInfo = DataLoadMetaInfo(headers = localHeaders)
            )
        )
        var onRemoteUpdatedCalled = false

        val repoFlow = localFlow.asRepoFlow(
            dataLoadParams = DataLoadParams(),
            remoteFlow = {
                flowOf(
                    DataReadyState(
                        data = "remote-data",
                        metaInfo = DataLoadMetaInfo(headers = remoteHeaders)
                    )
                )
            },
            onRemoteUpdated = {
                onRemoteUpdatedCalled = true
            }
        )

        repoFlow.filter {
            it.localState is DataReadyState && it.remoteState is DataReadyState
        }.test(timeout = 5.seconds) {
            val dataReady = awaitItem()

            assertTrue(dataReady is DataReadyState)
            assertEquals("local-data", dataReady.data)
            assertEquals("remote-data", (dataReady.remoteState as? DataReadyState)?.data)
            cancelAndIgnoreRemainingEvents()
        }

        assertFalse(onRemoteUpdatedCalled)
    }

}