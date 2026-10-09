package com.ustadmobile.libcache

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.ustadmobile.ihttp.headers.IHttpHeader
import com.ustadmobile.ihttp.request.iRequestBuilder
import com.ustadmobile.ihttp.request.requestBuilder
import com.ustadmobile.ihttp.response.IHttpResponse
import com.ustadmobile.libcache.cachecontrol.CacheControlFreshnessCheckerImpl
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.db.entities.RetentionLock
import com.ustadmobile.libcache.downloader.EnqueuePinPublicationPrepareUseCaseJvm
import com.ustadmobile.libcache.headers.requireIntegrity
import com.ustadmobile.libcache.integrity.sha256Integrity
import com.ustadmobile.libcache.io.RangeInputStream
import com.ustadmobile.libcache.io.uncompress
import com.ustadmobile.libcache.logging.NapierLoggingAdapter
import com.ustadmobile.libcache.md5.Md5Digest
import com.ustadmobile.libcache.md5.urlHash
import com.ustadmobile.libcache.md5.urlKey
import com.ustadmobile.libcache.novarysearch.normalizeForNoVarySearchIfNotNull
import com.ustadmobile.libcache.response.StringResponse
import com.ustadmobile.libcache.response.bodyAsUncompressedSourceIfContentEncoded
import com.ustadmobile.libcache.util.LaunchNoVarySearchConstants.LAUNCH_LINK_NO_VARY_HEADER
import com.ustadmobile.libcache.util.awaitUpdatesCommitted
import com.ustadmobile.libcache.util.initNapierLog
import com.ustadmobile.libcache.util.newFileFromResource
import com.ustadmobile.libcache.util.storeFileAsUrl
import io.ktor.http.Headers
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlinx.io.asInputStream
import kotlinx.io.files.Path
import kotlinx.io.readByteArray
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.openeel.libxxhash.jvmimpl.XXStringHasherCommonJvm
import java.io.ByteArrayInputStream
import java.io.File
import java.io.SequenceInputStream
import java.security.MessageDigest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 05/Aug/2026: closing the database using Room 2.8.4's bundled driver is causing JVM crashes.
 */
class UstadCacheJvmTest {

    @get:Rule
    val tempDir = TemporaryFolder()

    private lateinit var rootDir: File

    private lateinit var temporaryFolderPathsProvider: CachePathsProvider

    private lateinit var cachePaths: CachePaths

    @BeforeTest
    fun setup(){
        rootDir = tempDir.newFolder()
        initNapierLog()
        val rootPath = Path(rootDir.absolutePath)
        cachePaths = CachePaths(
            tmpWorkPath = Path(rootPath, "tmpWork"),
            persistentPath = Path(rootPath, "persistent"),
            cachePath = Path(rootPath, "cache")
        )
        temporaryFolderPathsProvider = CachePathsProvider {
            cachePaths
        }
    }

    data class AssertCacheHitResult(
        val response: IHttpResponse
    )

    private fun UstadCache.assertIsCacheHit(
        testFile: File,
        testUrl: String,
        retrieveUrl: String = testUrl,
        mimeType: String,
        expectedContentEncoding: String? = null,
        requestHeaders: List<IHttpHeader> = emptyList(),
        extraHeaders: Headers? = null,
    ): AssertCacheHitResult {
        val request = requestBuilder {
            url = retrieveUrl
            requestHeaders.forEach {
                header(it.name, it.value)
            }
        }

        //Check response body content matches
        val cacheResponse = runBlocking { retrieve(request) }
        assertNotNull(cacheResponse, "cache response for $testUrl is not null")
        val bodyBytesRaw = cacheResponse.bodyAsSource()!!.readByteArray()

        val bodyBytesDecoded = ByteArrayInputStream(bodyBytesRaw).uncompress(
            CompressionType.byHeaderVal(cacheResponse.headers["content-encoding"])
        ).readAllBytes()

        Assert.assertArrayEquals(testFile.readBytes(), bodyBytesDecoded)

        val dataSha256 = MessageDigest.getInstance("SHA-256").also {
            it.update(testFile.readBytes())
        }.digest()

        val integrityHeaderVal = cacheResponse.headers.requireIntegrity()
        Assert.assertEquals(sha256Integrity(dataSha256), integrityHeaderVal)

        //If content-encoding was set, then content-length will not be the same as input file
        assertEquals(bodyBytesRaw.size.toLong(), cacheResponse.headers["content-length"]?.toLong())
        assertEquals(mimeType, cacheResponse.headers["content-type"])

        if(expectedContentEncoding != null) {
            val numEncodingHeaders = cacheResponse.headers.getAllByName("content-encoding").size
            if(expectedContentEncoding == "identity") {
                assertTrue(cacheResponse.headers["content-encoding"].let { it == null || it == "identity" },
                    "Content-encoding for $testUrl should be identity - can have no header, or can be set to identity")
                assertTrue(numEncodingHeaders == 1 || numEncodingHeaders == 0)
            }else {
                assertEquals(expectedContentEncoding, cacheResponse.headers["content-encoding"],
                    "Content-encoding for $testUrl should be $expectedContentEncoding")
                assertEquals(1, numEncodingHeaders)
            }
        }

        extraHeaders?.also { headers ->
            headers.forEach { name, values ->
                val allResponseHeaders = cacheResponse.headers.getAllByName(name)
                values.forEach { extraHeaderVal ->
                    assertTrue(
                        actual = allResponseHeaders.any { it == extraHeaderVal },
                        message = "Extra header $name with value $extraHeaderVal should be present"
                    )
                }
            }
        }

        return AssertCacheHitResult(response = cacheResponse)
    }

    private fun UstadCache.assertCanStoreAndRetrieveFileAsCacheHit(
        testFile: File,
        testUrl: String,
        retrieveUrl: String = testUrl,
        mimeType: String,
        expectedContentEncoding: String? = null,
        requestHeaders: List<IHttpHeader> = emptyList(),
        extraHeaders: Headers? = null,
    ): AssertCacheHitResult {
        runBlocking {
            storeFileAsUrl(
                testFile = testFile,
                testUrl = testUrl,
                mimeType = mimeType,
                requestHeaders = requestHeaders,
                extraHeaders = extraHeaders,
            )
        }

        return assertIsCacheHit(
            testFile = testFile,
            testUrl = testUrl,
            retrieveUrl = retrieveUrl,
            mimeType = mimeType,
            expectedContentEncoding = expectedContentEncoding,
            requestHeaders = requestHeaders,
            extraHeaders = extraHeaders,
        )
    }

    private inline fun UstadCacheDb.useDb(
        block: (UstadCacheDb) -> Unit
    ) {
        try {
            block(this)
        }finally {
            //this.close()
        }
    }


    data class FileCanBeCachedAndRetrievedContext(
        val cacheDb: UstadCacheDb,
        val cache: UstadCacheImpl,
        val createdLocks: List<Pair<EntryLockRequest, RetentionLock>>,
    )

    private fun withTestCache(
        databaseDir: File,
        block: (Pair<UstadCacheDb, UstadCacheImpl>) -> Unit,
    ) {
        val dbFile = File(databaseDir, DB_FILENAME)
        databaseDir.takeIf { !it.exists() }?.mkdirs()

        val cacheDb = Room.databaseBuilder<UstadCacheDb>(dbFile.absolutePath)
            .setDriver(BundledSQLiteDriver())
            .build()

        val ustadCache = UstadCacheImpl(
            pathsProvider = temporaryFolderPathsProvider,
            db = cacheDb,
            xxStringHasher = XXStringHasherCommonJvm(),
            logger = NapierLoggingAdapter(),
            enqueuePinPublicationPrepareUseCase = EnqueuePinPublicationPrepareUseCaseJvm(
                cacheDb, XXStringHasherCommonJvm()
            ),
            freshnessChecker = CacheControlFreshnessCheckerImpl(),
        )

        try {
            block(Pair(cacheDb, ustadCache))
        }finally {
            ustadCache.close()
            //See class KDoc
            //cacheDb.close()
        }
    }


    private fun assertFileCanBeCachedAndRetrieved(
        testDataContent: File,
        testUrl: String,
        retrieveUrl: String = testUrl,
        mimeType: String,
        expectContentEncoding: String? = null,
        requestHeaders: List<IHttpHeader> = emptyList(),
        createLock: Boolean = false,
        extraHeaders: Headers? = null,
        databaseDir: File = tempDir.newFolder(),
        block: FileCanBeCachedAndRetrievedContext.() -> Unit = { },
    ) {
        withTestCache(databaseDir) { (cacheDb, ustadCache) ->
            val createdLocks = if(createLock) {
                runBlocking { ustadCache.addRetentionLocks(listOf(EntryLockRequest(testUrl))) }
            }else {
                emptyList()
            }

            val result = ustadCache.assertCanStoreAndRetrieveFileAsCacheHit(
                testFile =testDataContent,
                testUrl = testUrl,
                retrieveUrl = retrieveUrl,
                mimeType = mimeType,
                expectedContentEncoding = expectContentEncoding,
                requestHeaders = requestHeaders,
                extraHeaders = extraHeaders,
            )

            val urlForKey = Url(retrieveUrl).normalizeForNoVarySearchIfNotNull(
                result.response.headers["No-Vary-Search"]
            )

            val cacheEntryInDb = runBlocking {
                ustadCache.awaitUpdatesCommitted()
                cacheDb.cacheEntryDao.findEntryByKey(Md5Digest().urlHash(urlForKey))
            }
            assertNotNull(cacheEntryInDb)
            val expectedPath = if(createLock) {
                cachePaths.persistentPath
            }else {
                cachePaths.cachePath
            }

            assertTrue(cacheEntryInDb.storageUri.startsWith(expectedPath.toString()),
                "Cache entry is stored in expected directory (createLock=$createLock, " +
                        "expected path = $expectedPath, actual dir = ${cacheEntryInDb.storageUri}")

            block(FileCanBeCachedAndRetrievedContext(cacheDb, ustadCache, createdLocks))
        }
    }

    @Test
    fun givenNonCompressableFileStored_whenRequestMade_thenWillBeRetrievedAsCacheHitAndNotCompressed() {
        assertFileCanBeCachedAndRetrieved(
            testDataContent = tempDir.newFileFromResource(this::class.java, "/testfile1.png"),
            testUrl = "http://www.server.com/file.png",
            mimeType = "image/png",
            expectContentEncoding = "identity"
        )
    }

    @Test
    fun givenLockedEntryStored_whenRequestMade_thenWillBeRetrievedAsCacheHitAndSavedInPersistentPath() {
        assertFileCanBeCachedAndRetrieved(
            testDataContent = tempDir.newFileFromResource(this::class.java, "/testfile1.png"),
            testUrl = "http://www.server.com/file.png",
            mimeType = "image/png",
            expectContentEncoding = "identity",
            createLock = true,
        )
    }

    @Test
    fun givenEntryNotLocked_whenLockAdded_thenWillBeMovedToPersistentDir() {
        val url = "http://www.server.com/file.png"
        assertFileCanBeCachedAndRetrieved(
            testDataContent = tempDir.newFileFromResource(this::class.java, "/testfile1.png"),
            testUrl = "http://www.server.com/file.png",
            mimeType = "image/png",
            expectContentEncoding = "identity"
        ) {
            runBlocking { cache.addRetentionLocks(listOf(EntryLockRequest(url))) }
            val entry = runBlocking { cache.getCacheEntry(url) }
            assertEquals(
                expected = entry?.storageUri?.startsWith(cachePaths.persistentPath.toString()),
                actual = true,
                "After adding lock, entry should be in persistent path"
            )
        }
    }

    @Test
    fun givenEntryLocked_whenLockRemoved_thenWillBeMovedToCacheDir() {
        val url = "http://www.server.com/file.png"
        assertFileCanBeCachedAndRetrieved(
            testDataContent = tempDir.newFileFromResource(this::class.java, "/testfile1.png"),
            testUrl = "http://www.server.com/file.png",
            mimeType = "image/png",
            expectContentEncoding = "identity",
            createLock = true,
        ) {
            runBlocking {
                cache.removeRetentionLocks(
                    createdLocks.map {
                        RemoveLockRequest(url, it.second.lockId)
                    }
                )

                val entry = cache.getCacheEntry(url)
                assertEquals(
                    expected = entry?.storageUri?.startsWith(cachePaths.cachePath.toString()),
                    actual = true,
                    message = "After adding lock, entry should be in persistent path"
                )
            }
        }
    }

    @Test
    fun givenCompressableFileStored_whenRequestMade_thenWillBeRetrievedAsCacheHitAndBeCompressed() {
        assertFileCanBeCachedAndRetrieved(
            testDataContent = tempDir.newFileFromResource(this::class.java, "/ustadmobile-epub.js"),
            testUrl = "http://www.server.com/ustadmobile-epub.js",
            mimeType = "application/javascript",
            expectContentEncoding = "gzip",
            requestHeaders = listOf(IHttpHeader.fromNameAndValue("accept-encoding", "gzip, br, deflate"))
        )
    }

    @Test
    fun givenCompressableFileStored_whenRequestMadeWithoutAcceptEncoding_thenWillBeRetrievedAsCacheHitAndBeCompressed() {
        assertFileCanBeCachedAndRetrieved(
            testDataContent = tempDir.newFileFromResource(this::class.java, "/ustadmobile-epub.js"),
            testUrl = "http://www.server.com/ustadmobile-epub.js",
            mimeType = "application/javascript",
            expectContentEncoding = "identity",
        )
    }

    @Test
    fun givenEmptyFileStored_whenRequestMade_thenWillBeRetrievedAsCacheHit() {
        assertFileCanBeCachedAndRetrieved(
            testDataContent = tempDir.newFile(),
            testUrl = "http://www.server.com/blank.txt",
            mimeType = "text/plain"
        )
    }

    @Test
    fun givenResponseIsUpdated_whenRetrieved_thenLatestResponseWillBeReturned(){
        Room.databaseBuilder<UstadCacheDb>(
            tempDir.newFile("cachetest.db").absolutePath
        ).setDriver(BundledSQLiteDriver()).build().useDb { cacheDb ->
            val ustadCache = UstadCacheImpl(
                pathsProvider = temporaryFolderPathsProvider,
                xxStringHasher = XXStringHasherCommonJvm(),
                db = cacheDb,
                enqueuePinPublicationPrepareUseCase = EnqueuePinPublicationPrepareUseCaseJvm(
                    cacheDb, XXStringHasherCommonJvm()
                ),
                freshnessChecker = CacheControlFreshnessCheckerImpl(),
            )

            val url = "http://server.com/file.css"
            val payloads = listOf("font-weight: bold", "font-weight: bold !important")
            runBlocking {
                payloads.forEachIndexed { _, payload ->
                    ustadCache.store(listOf(
                        iRequestBuilder(url).let {
                            CacheEntryToStore(
                                request = it,
                                response = StringResponse(
                                    request = it,
                                    mimeType = "text/css",
                                    body = payload,
                                )
                            )
                        }
                    ))
                }

                val response = ustadCache.retrieve(iRequestBuilder(url))
                val responseBytes = response?.bodyAsUncompressedSourceIfContentEncoded()
                    ?.asInputStream()?.readAllBytes()
                val responseStr = responseBytes?.let { String(it) }
                assertEquals(payloads.last(), responseStr)
            }
        }
    }


    @Test
    fun givenEntryNotStored_whenRetrieved_thenWillReturnNull() {
        Room.databaseBuilder<UstadCacheDb>(
            tempDir.newFile("cachetest.db").absolutePath
        ).setDriver(BundledSQLiteDriver()).build().useDb { cacheDb ->
            val ustadCache = UstadCacheImpl(
                pathsProvider = temporaryFolderPathsProvider,
                db = cacheDb,
                xxStringHasher = XXStringHasherCommonJvm(),
                enqueuePinPublicationPrepareUseCase = EnqueuePinPublicationPrepareUseCaseJvm(
                    cacheDb, XXStringHasherCommonJvm()
                ),
                freshnessChecker = CacheControlFreshnessCheckerImpl(),
            )

            val url = "http://server.com/file.css"
            assertNull(runBlocking { ustadCache.retrieve(iRequestBuilder(url)) } )
        }
    }

    @Test
    fun givenResponseIsNotUpdated_whenStored_thenWillUpdateLastAccessAndValidationTime() {
        Room.databaseBuilder<UstadCacheDb>(
            tempDir.newFile("cachetest.db").absolutePath
        ).setDriver(BundledSQLiteDriver()).build().useDb { cacheDb ->
            val ustadCache = UstadCacheImpl(
                pathsProvider = temporaryFolderPathsProvider,
                db = cacheDb,
                xxStringHasher = XXStringHasherCommonJvm(),
                enqueuePinPublicationPrepareUseCase = EnqueuePinPublicationPrepareUseCaseJvm(
                    cacheDb, XXStringHasherCommonJvm()
                ),
                freshnessChecker = CacheControlFreshnessCheckerImpl(),
            )

            val url = "http://server.com/file.css"
            val tmpFile = tempDir.newFile().also {
                it.writeText("font-weight: bold")
            }

            val md5Digest = Md5Digest()
            val entryAfterStored = (1..2).map {
                ustadCache.assertCanStoreAndRetrieveFileAsCacheHit(
                    testFile = tmpFile,
                    testUrl = url,
                    mimeType = "text/css"
                )

                runBlocking {
                    ustadCache.awaitUpdatesCommitted()
                    cacheDb.cacheEntryDao.findEntryByKey(md5Digest.urlKey(url))
                }
            }


            assertTrue(entryAfterStored.last()!!.lastValidated > entryAfterStored.first()!!.lastValidated,
                message = "Last validated time should be updated after ")

            //Cache tmp directory should not have any leftover files.
            val cacheTmpDir = File(rootDir, "tmpWork")

            assertTrue(cacheTmpDir.exists())
            assertEquals(0, cacheTmpDir.list()!!.size)
        }

    }

    @Test
    fun givenFileCachedAndStored_whenPartialRequestMade_thenWillReceivePartialData() {
        val testUrl = "http://www.server.com/file.png"
        assertFileCanBeCachedAndRetrieved(
            testDataContent = tempDir.newFileFromResource(this::class.java, "/testfile1.png"),
            testUrl = testUrl,
            mimeType = "image/png",
            expectContentEncoding = "identity"
        ) {
            val resourceBytes = this::class.java.getResourceAsStream(
                "/testfile1.png")!!.readAllBytes()
            val etag = runBlocking {
                cache.retrieve(iRequestBuilder(testUrl))?.headers?.get("etag")
            }
            assertNotNull(etag)

            val partialResponse = runBlocking {
                cache.retrieve(iRequestBuilder(testUrl) {
                    header("Range", "bytes=1000-")
                    header("If-Range", etag)
                })
            }
            assertNotNull(partialResponse)
            assertEquals(206, partialResponse.responseCode)

            val partialResponseInput = partialResponse.bodyAsSource()!!.asInputStream()

            val combinedBytes = SequenceInputStream(
                RangeInputStream(ByteArrayInputStream(resourceBytes), 0, 999),
                partialResponseInput
            ).readAllBytes()

            assertTrue(resourceBytes.contentEquals(combinedBytes),
                "Combined partial response data should match original resource data")
        }
    }

    @Test
    fun givenFileCachedAndStored_whenPartialRequestMadeIfRangeNotMatched_thenWillReceiveFullResponse() {
        val testUrl = "http://www.server.com/file.png"
        assertFileCanBeCachedAndRetrieved(
            testDataContent = tempDir.newFileFromResource(this::class.java, "/testfile1.png"),
            testUrl = testUrl,
            mimeType = "image/png",
            expectContentEncoding = "identity"
        ) {
            val resourceBytes = this::class.java.getResourceAsStream(
                "/testfile1.png")!!.readAllBytes()
            val fullResponse = runBlocking {
                cache.retrieve(iRequestBuilder(testUrl) {
                    header("Range", "bytes=1000-")
                    header("If-Range", "something-else")
                })
            }
            assertNotNull(fullResponse)
            assertEquals(200, fullResponse.responseCode,
                "When if-range did not match etag, full response should be returned")

            val responseBytes = fullResponse.bodyAsSource()!!.asInputStream().readAllBytes()
            assertTrue(resourceBytes.contentEquals(responseBytes),
                "When if-range did not match actual etag, returned full response")
        }
    }


    @Test
    fun givenFileCachedAndStored_whenRequestHasCacheValidationHeaders_thenShouldRespond304NotModified() {
        val testUrl = "http://www.server.com/file.png"
        assertFileCanBeCachedAndRetrieved(
            testDataContent = tempDir.newFileFromResource(this::class.java, "/testfile1.png"),
            testUrl = testUrl,
            mimeType = "image/png",
            expectContentEncoding = "identity"
        ) {
            val fullResponse = runBlocking {
                cache.retrieve(iRequestBuilder(testUrl))
            }

            listOf(
                Pair("If-None-Match", "etag"), Pair("If-Modified-Since", "Last-Modified")
            ).forEach { (requestHeaderName, responseHeaderName) ->
                assertEquals(
                    expected = 304,
                    actual = runBlocking {
                        cache.retrieve(
                            request = iRequestBuilder(testUrl) {
                                header(
                                    headerName = requestHeaderName,
                                    headerVal = fullResponse!!.headers[responseHeaderName]!!
                                )
                            }
                        )!!.responseCode
                    }
                )
            }

            assertEquals(
                expected = 304,
                actual = runBlocking {
                    cache.retrieve(
                        request = iRequestBuilder(testUrl) {
                            header(
                                headerName = "If-None-Match",
                                headerVal = fullResponse!!.headers["etag"]!!
                            )
                            header(
                                headerName = "If-Modified-Since",
                                headerVal = fullResponse.headers["last-modified"]!!
                            )
                        }
                    )!!.responseCode
                }
            )
        }
    }

    @Test
    fun givenExtraHeaderSet_whenRetrieved_thenExtraHeaderIsAdded() {
        val testDbDir = tempDir.newFolder()
        val testFileContent = tempDir.newFileFromResource(this::class.java, "/testfile1.png")

        assertFileCanBeCachedAndRetrieved(
            testDataContent = testFileContent,
            testUrl = "http://www.server.com/file.png",
            mimeType = "image/png",
            extraHeaders = headersOf("No-Vary-Search", "params"),
            databaseDir = testDbDir,
        )

        withTestCache(testDbDir) { (_, ustadCache) ->
            ustadCache.assertIsCacheHit(
                testFile = testFileContent,
                "http://www.server.com/file.png",
                mimeType = "image/png",
                extraHeaders = headersOf("No-Vary-Search", "params"),
            )
        }
    }

    @Test
    fun givenNoVarySearchHeaderSet_whenRetrievedUsingMatchingUrl_thenIsCacheHit() {
        data class NoVarySearchCase(
            val storedAsUrl: String,
            val retrieveUrl: String,
            val noVarySearchHeader: String
        )

        listOf(
            NoVarySearchCase(
                "http://www.server.com/file.png",
                "http://www.server.com/file.png?ts=1",
                "params"
            ),

            NoVarySearchCase(
                "http://www.server.com/file.png?lang=en&lesson=2",
                "http://www.server.com/file.png?lang=en&lesson=2&actor=janedoe&endpoint=server",
                "key-order, params=(\"endpoint\" \"actor\")",
            ),
            NoVarySearchCase(
                "http://www.server.com/file.png?lang=en&lesson=2",
                "http://www.server.com/file.png?lang=en&lesson=2&actor=janedoe&endpoint=server",
                LAUNCH_LINK_NO_VARY_HEADER,
            )
        ).forEachIndexed { _, case ->
            val testFile = tempDir.newFileFromResource(this::class.java, "/testfile1.png")
            val dbDir =  tempDir.newFolder() //File("/home/mike/tmp/dbnovary$index")

            assertFileCanBeCachedAndRetrieved(
                testDataContent = testFile,
                testUrl = case.storedAsUrl,
                retrieveUrl = case.retrieveUrl,
                mimeType = "image/png",
                extraHeaders = headersOf("No-Vary-Search" to listOf(case.noVarySearchHeader)),
                databaseDir = dbDir,
            )

            withTestCache(dbDir) { (_, ustadCache) ->
                ustadCache.assertIsCacheHit(
                    testFile = testFile,
                    testUrl = case.storedAsUrl,
                    retrieveUrl = case.retrieveUrl,
                    mimeType = "image/png",
                    extraHeaders = headersOf("No-Vary-Search" to listOf(case.noVarySearchHeader)),
                )
            }
        }
    }


    companion object {

        const val DB_FILENAME = "cache.db"

    }

}