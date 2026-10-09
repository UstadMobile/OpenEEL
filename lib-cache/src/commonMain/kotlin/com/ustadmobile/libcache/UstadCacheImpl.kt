package com.ustadmobile.libcache

import androidx.room.Transactor.SQLiteTransactionType
import androidx.room.deferredTransaction
import androidx.room.useReaderConnection
import androidx.room.useWriterConnection
import com.ustadmobile.ihttp.headers.IHttpHeaders
import com.ustadmobile.ihttp.headers.MergedHeaders
import com.ustadmobile.ihttp.headers.asRawString
import com.ustadmobile.ihttp.headers.asString
import com.ustadmobile.ihttp.headers.mapHeaders
import com.ustadmobile.ihttp.iostreams.NullOutputStream
import com.ustadmobile.libcache.cachecontrol.ResponseValidityChecker
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.db.entities.CacheEntry
import com.ustadmobile.libcache.db.entities.CacheEntryAndMetadata
import com.ustadmobile.libcache.db.entities.RetentionLock
import com.ustadmobile.libcache.integrity.sha256Integrity
import com.ustadmobile.libcache.io.moveWithFallback
import com.ustadmobile.libcache.io.requireMetadata
import com.ustadmobile.libcache.io.useAndReadSha256
import com.ustadmobile.libcache.io.transferToAndGetSha256
import com.ustadmobile.libcache.io.uncompress
import com.ustadmobile.libcache.logging.UstadCacheLogger
import com.ustadmobile.libcache.md5.Md5Digest
import com.ustadmobile.libcache.md5.urlKey
import com.ustadmobile.ihttp.request.IHttpRequest
import com.ustadmobile.libcache.response.CacheResponse
import com.ustadmobile.ihttp.response.IHttpResponse
import com.ustadmobile.libcache.cachecontrol.CacheControlFreshnessChecker
import com.ustadmobile.libcache.db.entities.CacheEntryExtraHeaders
import com.ustadmobile.libcache.db.entities.TransferJobItemStatus
import com.ustadmobile.libcache.db.ext.getContentEntryAndMetaDataByKey
import com.ustadmobile.libcache.db.ext.makeHttpResponseHeaders
import com.ustadmobile.libcache.downloader.EnqueuePinPublicationPrepareUseCase
import com.ustadmobile.libcache.headers.hasCacheValidators
import com.ustadmobile.libcache.headers.integrity
import com.ustadmobile.libcache.md5.urlHash
import com.ustadmobile.libcache.novarysearch.NoVarySearch
import com.ustadmobile.libcache.novarysearch.normalizeForNoVarySearch
import com.ustadmobile.libcache.novarysearch.removeAllSearchParams
import com.ustadmobile.libcache.response.ByteArrayResponse
import com.ustadmobile.libcache.util.concurrentSafeMapOf
import com.ustadmobile.libcache.util.receivePending
import com.ustadmobile.libcache.util.sendAndUpdateBacklogSize
import com.ustadmobile.libcache.util.trySendAndUpdateBacklogSize
import io.github.reactivecircus.cache4k.Cache
import io.github.reactivecircus.cache4k.CacheEvent
import io.ktor.http.Headers
import io.ktor.http.Url
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.getAndUpdate
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.io.asSink
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.openeel.libutil.util.time.systemTimeInMillis
import org.openeel.libxxhash.XXStringHasher
import kotlin.also
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 *
 * @param sizeLimit A function that returns the current size limit for the cache. This will be
 *        invoked on the periodic trims that are run. The limit applies to evictable entries e.g.
 *        entries which do not have any retentionlock.
 */
@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
class UstadCacheImpl(
    private val fileSystem: FileSystem = SystemFileSystem,
    cacheName: String = "",
    private val pathsProvider: CachePathsProvider,
    private val db: UstadCacheDb,
    sizeLimit: () -> Long = { UstadCache.DEFAULT_SIZE_LIMIT },
    private val logger: UstadCacheLogger? = null,
    private val listener: UstadCache.CacheListener? = null,
    private val trimInterval: Int = 30_000,
    private val responseValidityChecker: ResponseValidityChecker = ResponseValidityChecker(),
    private val trimmer: UstadCacheTrimmer = UstadCacheTrimmer(
        db = db,
        fileSystem = fileSystem,
        logger = logger,
        sizeLimit = sizeLimit,
    ),
    override val storageCompressionFilter: CacheStorageCompressionFilter = DefaultCacheCompressionFilter(),
    private val xxStringHasher: XXStringHasher,
    private val enqueuePinPublicationPrepareUseCase: EnqueuePinPublicationPrepareUseCase,
    private val freshnessChecker: CacheControlFreshnessChecker,
    memoryCacheMaxEntries: Long = MEMORY_CACHE_DEFAULT_NUM_ENTRIES,
) : UstadCache {

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val tmpCounter = atomic(0)

    private val lockIdAtomic = atomic(Clock.System.now().toEpochMilliseconds())

    private val logPrefix = "UstadCache($cacheName):"

    /**
     * Data class that is used to track the status of a CacheEntryToStore as it is processed.
     *
     * @param cacheEntry the CacheEntry entity as it will be saved into the database
     * @param entryToStore the entryToStore request that provided as an argument to the store function
     * @param tmpFile the temporary file where data is being kept
     * @param responseHeaders the response headers (canonical) as they will be stored including the
     *        etag integrity values etc. that get added by default
     * @param tmpFileNeedsDeleted if true, then the tmpFile must be deleted before completing
     *        the store function. Can be false when responseBodyTmpLocalPath is provided, because
     *        then the responseBodyTmpLocalPath will be moved (not copied).
     * @param previousStorageUriToDelete if it is determined that new data will be replacing old
     *        data, then the previous body data will be deleted.
     *
     */
    private data class CacheEntryInProgress(
        val cacheEntry: CacheEntry,
        val entryToStore: CacheEntryToStore,
        val tmpFile: Path,
        val responseHeaders: IHttpHeaders,
        val tmpFileNeedsDeleted: Boolean = false,
        val lockId: Long = 0,
        val previousStorageUriToDelete: String? = null,
    )

    private val pendingCacheUpdates = concurrentSafeMapOf<String, CacheEvent.Updated<String, CacheEntryAndMetadata>>()

    sealed interface UpdateToCommit

    data class LastAccessedUpdate(
        val key: String,
        val accessTime: Long,
    ): UpdateToCommit

    data class CacheEntryUpdate(
        val event: CacheEvent.Updated<String, CacheEntryAndMetadata>
    ): UpdateToCommit

    /**
     * Updates that need to be committed to the database are put onto this channel to a) avoid the
     * client having to wait for the database update to finish and b) batch updates together where
     * possible.
     */
    private val updatesToCommitChannel = Channel<UpdateToCommit>(
        capacity = Channel.UNLIMITED,
    )

    private val _updateBacklogSize = MutableStateFlow(0)

    /**
     * Allows tests to wait until all updates have been committed to the database before making
     * assertions on the database.
     */
    internal val updateBacklogSize = _updateBacklogSize.asStateFlow()

    /**
     *
     */
    private val urlWithoutSearchToKeysCache = Cache.Builder<Url, Set<String>>().build()

    private val urlWithoutSearchToKeysWriteLock = ReentrantLock()

    private val closed = atomic(false)


    private val memoryCache = Cache.Builder<String, CacheEntryAndMetadata>()
        .maximumCacheSize(memoryCacheMaxEntries)
        .eventListener { event ->
            when(event) {
                is CacheEvent.Updated -> {
                    pendingCacheUpdates.compute(event.key) { _, prev ->
                        if(prev == null) {
                            event
                        }else {
                            CacheEvent.Updated(event.key, prev.oldValue, event.newValue)
                        }
                    }
                    updatesToCommitChannel.trySendAndUpdateBacklogSize(
                        CacheEntryUpdate(event), _updateBacklogSize,
                    )

                    event.newValue.entry?.urlWithoutSearch?.also { urlWithoutSearch ->
                        urlWithoutSearchToKeysWriteLock.withLock {
                            urlWithoutSearchToKeysCache.put(
                                key = urlWithoutSearch,
                                value = urlWithoutSearchToKeysCache.get(
                                    key = urlWithoutSearch
                                ).orEmpty() + event.key
                            )
                        }
                    }
                }

                else -> {
                    //do nothing
                }
            }
        }
        .build()



    suspend fun Cache<Url, Set<String>>.getOrLoadKeysFromDbAndPending(
        urlWithoutSearch: Url
    ) : Set<String> {
        return get(urlWithoutSearch) {
            //Load from database plus any pending entries.
            db.cacheEntryDao.findKeysByUrlWithoutSearchHash(
                Md5Digest().urlHash(urlWithoutSearch)
            ).toSet() + pendingCacheUpdates.mapNotNull { update ->
                update.key.takeIf {
                    update.value.newValue.entry?.urlWithoutSearch == urlWithoutSearch
                }
            }
        }
    }

    /**
     * Goes through a list of cache keys (urlWithoutSearchMatchingKeys) and returns the first
     * for which the corresponding cache entry url matches after applying the no-vary-search url
     * normalization (as per the No-Vary-Search header for the CacheEntry with the given key).
     *
     * @param requestUrl the url being requested
     * @param urlWithoutSearchMatchingKeys cache keys to go through e.g. from urlWithoutSearchToKeysCache
     *
     * @return A CacheEntryAndMetadata if there is a CacheEntry matching the request url (after
     *         applying no-vary-search normalization)
     */
    private fun Cache<String, CacheEntryAndMetadata>.selectFromUrlWithoutSearchKeys(
        requestUrl: Url,
        urlWithoutSearchMatchingKeys: Set<String>
    ) : CacheEntryAndMetadata? {
        return urlWithoutSearchMatchingKeys.firstNotNullOfOrNull { candidateKey ->
            val candidate = get(candidateKey)

            val candidateEntry = candidate?.entry ?: return@firstNotNullOfOrNull null

            val noVaryHeader = IHttpHeaders.fromString(
                candidateEntry.responseHeaders
            ).get("No-Vary-Search")?.let {
                NoVarySearch.parse(it)
            } ?: return@firstNotNullOfOrNull null

            candidate.takeIf {
                requestUrl.normalizeForNoVarySearch(noVaryHeader) == candidateEntry.url
            }
        }
    }

    /**
     * Get the CacheEntryAndMetadata for the given request url
     *
     * @param url request url
     */
    private suspend fun Cache<String, CacheEntryAndMetadata>.getOrLoadFromDb(
        url: Url,
    ): CacheEntryAndMetadata {
        val md5 = Md5Digest()
        val urlHash = md5.urlHash(url)

        val entryInMemory = get(urlHash)

        /* When No-Vary-Search is used the request url and the url used for the real cache key are
         * different, so we should only return this immediately only if the actual CacheEntry is not null.
         */
        if(entryInMemory?.entry != null)
            return entryInMemory

        //Check for loaded NoVarySearch matches
        val urlWithoutSearch = url.removeAllSearchParams()
        val urlWithoutSearchMatchKeys = urlWithoutSearchToKeysCache.getOrLoadKeysFromDbAndPending(
            urlWithoutSearch
        )

        val inMemoryUrlWithoutSearchMatch = selectFromUrlWithoutSearchKeys(
            url, urlWithoutSearchMatchKeys
        )

        if(inMemoryUrlWithoutSearchMatch != null)
            return inMemoryUrlWithoutSearchMatch

        /*
         * When an entry matches using NoVarySearch its URL (as per CacheEntry.url) will not match
         * the request URL. We must not corrupt the memory cache by storing a CacheEntry using the
         * wrong key.
         */
        var noVarySearchMatchFromDb: CacheEntryAndMetadata? = null

        val loadedEntry = get(urlHash) {
            db.useReaderConnection { con ->
                con.deferredTransaction {
                    val entryForUrl = db.cacheEntryDao.findEntryByKey(urlHash)

                    /**
                     * If there is a direct match for the exact url, take it, we're done.
                     *
                     * Future refinement: check for validity to ensure that a more updated no-vary-search
                     * result would not be hidden by an older direct match if/as applicable after headers
                     * were changed/updated.
                     */
                    if(entryForUrl != null) {
                        return@deferredTransaction db.getContentEntryAndMetaDataByKey(
                            urlHash, entryForUrl
                        )
                    }

                    //Load any potential urlWithoutSearch matches into the in-memory cache
                    urlWithoutSearchMatchKeys.forEach { key ->
                        if(key != urlHash) {
                            get(key) {
                                db.getContentEntryAndMetaDataByKey(key, null)
                            }
                        }
                    }

                    noVarySearchMatchFromDb = selectFromUrlWithoutSearchKeys(
                        url, urlWithoutSearchMatchKeys
                    )

                    CacheEntryAndMetadata(
                        urlKey = urlHash,
                        entry = db.cacheEntryDao.findEntryByKey(urlHash),
                        locks = db.retentionLockDao.findByKey(urlHash),
                        extraHeaders = db.cacheEntryExtraHeadersDao.findByKey(urlHash),
                    )
                }
            }
        }

        return noVarySearchMatchFromDb ?: loadedEntry
    }

    suspend fun Cache<String, CacheEntryAndMetadata>.update(
        url: Url,
        update: (CacheEntryAndMetadata) -> CacheEntryAndMetadata,
    ) {
        val urlHash = Md5Digest().urlHash(url)
        val current = getOrLoadFromDb(url)
        current.mutex.withLock {
            val newVal = update(current)
            put(urlHash, newVal)
        }
    }

    init {
        scope.launch {
            while(isActive) {
                delay(trimInterval.milliseconds)
                trimmer.trim()
            }
        }

        scope.launch {
            trimmer.evictedEntriesFlow.collect { evictedEntries ->
                evictedEntries.forEach { evictedKey ->
                    memoryCache.invalidate(evictedKey)
                }
            }
        }

        scope.launch {
            while(isActive) {
                val updatesToCommit = listOf(
                    updatesToCommitChannel.receive()
                ) + updatesToCommitChannel.receivePending(maxItems = 100)

                try {
                    val cacheUpdateEvents = updatesToCommit.mapNotNull {
                        (it as? CacheEntryUpdate)?.event
                    }

                    val lastAccessedUpdates = updatesToCommit.filterIsInstance<LastAccessedUpdate>()

                    //Ideally this would be done by creating a map

                    db.useWriterConnection { con ->
                        con.withTransaction(SQLiteTransactionType.IMMEDIATE) {
                            db.cacheEntryDao.upsertList(
                                entry = cacheUpdateEvents.mapNotNull { it.newValue.entry }
                            )

                            val newLocks = cacheUpdateEvents.flatMap { event ->
                                event.newValue.locks.filter { newLock ->
                                    !event.oldValue.locks.any { it.lockId == newLock.lockId }
                                }
                            }

                            val deletedLocks = cacheUpdateEvents.flatMap { event ->
                                event.oldValue.locks.filter { oldLock ->
                                    !event.newValue.locks.any { it.lockId == oldLock.lockId }
                                }
                            }

                            if(newLocks.isNotEmpty())
                                db.retentionLockDao.upsertList(newLocks)

                            if(deletedLocks.isNotEmpty())
                                db.retentionLockDao.delete(deletedLocks)

                            val extraHeaderChanges = cacheUpdateEvents.mapNotNull { evt ->
                                when {
                                    //There are no extra headers
                                    evt.newValue.extraHeaders == null && evt.oldValue.extraHeaders == null -> {
                                        null
                                    }

                                    //There is no update - same as before
                                    evt.newValue.extraHeaders == evt.oldValue.extraHeaders -> {
                                        null
                                    }

                                    else -> {
                                        evt.oldValue.extraHeaders
                                    }
                                }
                            }

                            if(extraHeaderChanges.isNotEmpty())
                                db.cacheEntryExtraHeadersDao.upsertList(extraHeaderChanges)

                            cacheUpdateEvents.forEach {
                                pendingCacheUpdates.remove(it.key)
                            }

                            lastAccessedUpdates.forEach {
                                db.cacheEntryDao.updateLastAccessedTime(it.key, it.accessTime)
                            }
                        }
                    }

                    _updateBacklogSize.update { it - updatesToCommit.size }
                }catch(e: Throwable) {
                    logger?.w(LOG_TAG, throwable = e) { "$logPrefix: exception committing updates to database"}
                    if(isActive)
                        delay(COMMIT_RETRY_DELAY.milliseconds)
                }
            }
        }
    }

    override suspend fun store(
        storeRequest: List<CacheEntryToStore>,
        progressListener: StoreProgressListener?
    ): List<StoreResult> {
        assertNotClosed()
        val md5Digest = Md5Digest()
        val timeNow = Clock.System.now().toEpochMilliseconds()
        val entryPaths = pathsProvider()

        try {
            logger?.d(LOG_TAG) { "$logPrefix store request for ${storeRequest.size} entries" }

            /**
             * Go through everything that is requested to be stored: create a list of CacheEntryInProgress
             * which contains the CacheEntry entity (headers, request status, etc) and a tmp
             * file containing the request body. Run checksums as needed to get a SHA256 checksum
             * for every response body.
             */
            val entriesWithTmpFileAndIntegrityInfo = storeRequest.map { entryToStore ->
                val response = entryToStore.response
                val cacheKey = md5Digest.urlHash(Url(entryToStore.request.url))
                val extraHeaders = memoryCache.get(cacheKey)?.extraHeaders
                val responseHeaders = if(extraHeaders != null) {
                    MergedHeaders(
                        IHttpHeaders.fromString(extraHeaders.extraHeaders),
                        response.headers
                    )
                }else {
                    response.headers
                }

                fileSystem.createDirectories(entryPaths.tmpWorkPath)
                val tmpFile = Path(entryPaths.tmpWorkPath,
                    "${tmpCounter.incrementAndGet()}.tmp")
                val url = entryToStore.request.url
                val storeCompressionType = storageCompressionFilter(
                    url = entryToStore.request.url,
                    requestHeaders = entryToStore.request.headers,
                    responseHeaders = entryToStore.response.headers
                )
                val responseCompression = CompressionType.byHeaderVal(
                    entryToStore.response.headers["content-encoding"]
                )
                val overrideHeaders = mutableMapOf<String, List<String>>()

                @Suppress("ArrayInDataClass")
                data class Sha256AndInflatedSize(val sha256: ByteArray?, val inflatedSize: Long)

                val (sha256IntegrityFromTransfer, uncompressedSize) = if(
                    entryToStore.responseBodyTmpLocalPath != null && storeCompressionType == responseCompression
                ) {
                    //If the entry to store is in a temporary path where it is acceptable to just
                    //move the file into the cache, and it is already compressed with the desired
                    // compression type for storage, then we will move (instead of copying) the file
                    fileSystem.moveWithFallback(entryToStore.responseBodyTmpLocalPath, tmpFile)
                    val inflatedSize = if(storeCompressionType == CompressionType.NONE) {
                        fileSystem.requireMetadata(tmpFile).size
                    }else {
                        //"transfer" to a nulloutputstream sink to count uncompressed size
                        fileSystem.source(tmpFile).buffered()
                            .uncompress(storeCompressionType).transferTo(NullOutputStream().asSink())
                    }

                    Sha256AndInflatedSize(null, inflatedSize)
                }else {
                    val bodySource = response.bodyAsSource()

                    if(bodySource == null) {
                        val e = IllegalArgumentException("Response for $url has " +
                                "no body. That should not have been stored in cache. Something badly wrong.")
                        logger?.e(LOG_TAG, "$logPrefix BodySource for ${entryToStore.request.url} is null", e)
                        throw e
                    }

                    val transferResult = bodySource.transferToAndGetSha256(tmpFile,
                        responseCompression, storeCompressionType)
                    overrideHeaders["content-encoding"] = listOf(storeCompressionType.headerVal)
                    overrideHeaders["content-length"] = listOf(fileSystem.requireMetadata(tmpFile).size.toString())

                    Sha256AndInflatedSize(transferResult.sha256, transferResult.transferred)
                }

                val integrityFromHeaders = if(entryToStore.skipChecksumIfProvided)
                    response.headers.integrity()
                else
                    null

                val integrity = sha256IntegrityFromTransfer?.let { sha256Integrity(it) }
                    ?: integrityFromHeaders
                    ?: sha256Integrity(fileSystem.source(tmpFile).buffered().useAndReadSha256())

                val effectiveHeaders = if(overrideHeaders.isNotEmpty()) {
                    MergedHeaders(IHttpHeaders.fromMap(overrideHeaders), responseHeaders)
                }else {
                    responseHeaders
                }

                logger?.v(LOG_TAG, "$logPrefix copied request data for $url to $tmpFile (integrity=$integrity)")

                val noVarySearchHeader = effectiveHeaders["No-Vary-Search"]
                val urlForKey = if (noVarySearchHeader != null) {
                    Url(entryToStore.request.url).normalizeForNoVarySearch(
                        NoVarySearch.parse(noVarySearchHeader)
                    )
                }else {
                    Url(entryToStore.request.url)
                }

                val urlWithoutSearch = if(noVarySearchHeader != null) {
                    Url(url).removeAllSearchParams()
                }else {
                    null
                }

                CacheEntryInProgress(
                    cacheEntry = CacheEntry(
                        key = md5Digest.urlHash(urlForKey),
                        url = urlForKey,
                        urlWithoutSearch = urlWithoutSearch,
                        urlWithoutSearchHash = urlWithoutSearch?.let { md5Digest.urlHash(it) },
                        integrity = integrity,
                        statusCode = entryToStore.response.responseCode,
                        responseHeaders = effectiveHeaders.asString(),
                        lastValidated = timeNow,
                        lastAccessed = timeNow,
                        uncompressedSize = uncompressedSize
                    ),
                    entryToStore = entryToStore,
                    tmpFile = tmpFile,
                    responseHeaders = entryToStore.response.headers,
                )
            }

            val processedEntries = entriesWithTmpFileAndIntegrityInfo.map { entryInProgress ->
                val key = entryInProgress.cacheEntry.key
                val entryInCache = memoryCache.getOrLoadFromDb(entryInProgress.cacheEntry.url)

                val storedEntry = entryInCache.entry

                val storedEntryHeaders = storedEntry?.responseHeaders?.let {
                    IHttpHeaders.fromString(it)
                }

                val etagOrLastModifiedMatches = if(storedEntryHeaders != null) {
                    responseValidityChecker.isMatchingEtagOrLastModified(
                        storedEntryHeaders, entryInProgress.entryToStore.response.headers
                    )
                }else {
                    false
                }

                if(storedEntry != null && etagOrLastModifiedMatches && storedEntryHeaders != null) {
                    /* If the entry is already saved and still valid. We will not store the body,
                     * but we will update the CacheEntry so that the last validated and last accessed
                     * times are updated.
                     *
                     * Because the body data will not be modified, the content-length and
                     * content-encoding MUST NOT be changed.
                     */
                    val overrideHeaders = buildMap {
                        NOT_MODIFIED_IGNORE_HEADERS.forEach { headerName ->
                            storedEntryHeaders[headerName]?.also { storedEntryHeaderVal ->
                                put(headerName, listOf(storedEntryHeaderVal))
                            }
                        }
                    }

                    entryInProgress.copy(
                        cacheEntry = entryInProgress.cacheEntry.copy(
                            storageUri = storedEntry.storageUri,
                            storageSize = storedEntry.storageSize,
                            responseHeaders = MergedHeaders(
                                IHttpHeaders.fromMap(overrideHeaders),
                                entryInProgress.responseHeaders,
                                storedEntryHeaders,
                            ).asString()
                        )
                    ).also {
                        memoryCache.put(
                            key = key,
                            value = entryInCache.copy(entry = it.cacheEntry)
                        )

                        fileSystem.delete(entryInProgress.tmpFile, mustExist = false)
                    }
                }else {
                    //Entry does not validate,
                    //The new entry does not validate, so we will need to store the new body.
                    val destPaths = pathsProvider()
                    val destPathParent = if(
                        entryInCache.locks.isNotEmpty() ||
                        entryInProgress.entryToStore.createRetentionLock
                    ) {
                        destPaths.persistentPath
                    }else {
                        destPaths.cachePath
                    }
                    fileSystem.createDirectories(destPathParent)
                    val destPath = Path(
                        base = destPathParent.toString(),
                        Uuid.random().toString()
                    )
                    fileSystem.moveWithFallback(entryInProgress.tmpFile, destPath)

                    entryInProgress.copy(
                        cacheEntry = entryInProgress.cacheEntry.copy(
                            storageUri = destPath.toString(),
                            storageSize = fileSystem.metadataOrNull(destPath)?.size ?: 0,
                        )
                    ).also {
                        memoryCache.put(
                            key = key,
                            value = entryInCache.copy(entry = it.cacheEntry)
                        )
                        fileSystem.delete(entryInProgress.tmpFile, mustExist = false)
                    }
                }
            }

            logger?.v(LOG_TAG) {
                "$logPrefix cacheEntries created ${entriesWithTmpFileAndIntegrityInfo.size} entries"
            }

            listener?.onEntriesStored(storeRequest)
            return processedEntries.map {
                StoreResult(
                    urlKey = it.cacheEntry.key,
                    request = it.entryToStore.request,
                    response = it.entryToStore.response,
                    integrity = it.cacheEntry.integrity!!,
                    storageSize = it.cacheEntry.storageSize,
                    lockId = it.lockId
                )
            }
        }catch(e: Throwable) {
            throw IllegalStateException("Could not cache", e)
        }
    }

    /**
     * Retrieve a response from the cache, if available. The response might be stale.
     *
     * NOTE: If we know in advance that a particular batch is going to be requested, we can run
     * a statusCheckCache to avoid running 100s-1000+ SQL queries for tiny jsons etc.
     *
     */
    override suspend fun retrieve(request: IHttpRequest): IHttpResponse? {
        logger?.i(LOG_TAG, "$logPrefix Retrieve ${request.url}")
        assertNotClosed()

        val url = Url(request.url)
        val entryAndLocks = memoryCache.getOrLoadFromDb(url)

        val entry = entryAndLocks.entry
        if(entry != null) {
            if(fileSystem.exists(Path(entry.storageUri))) {
                logger?.d(LOG_TAG, "$logPrefix FOUND ${request.url}")

                val timeNow = Clock.System.now().toEpochMilliseconds()

                updatesToCommitChannel.sendAndUpdateBacklogSize(
                    LastAccessedUpdate(entryAndLocks.urlKey,timeNow),
                    _updateBacklogSize
                )




                val responseHeaders = entry.makeHttpResponseHeaders(
                    entryAndLocks.extraHeaders?.extraHeaders
                )

                /*
                 * If the request received had its own explicitly set validation info AND the cached
                 * entry is fresh THEN we can reply an empty 304 not modified response.
                 */
                val requestHasValidators = request.headers.hasCacheValidators()
                val reply304 = requestHasValidators && freshnessChecker(
                    requestHeaders = request.headers,
                    responseHeaders = responseHeaders,
                    responseLastValidated = entry.lastValidated,
                    responseFirstStoredTime = systemTimeInMillis() //TODO: Set this properly
                ).isFresh

                return if(reply304) {
                    ByteArrayResponse(
                        request = request,
                        mimeType = responseHeaders["content-type"] ?: "application/octet-stream",
                        responseCode = 304,
                        body = ByteArray(0),
                    )
                }else {
                    CacheResponse(
                        fileSystem = fileSystem,
                        request = request,
                        headers = responseHeaders,
                        storageUri = entry.storageUri,
                        httpResponseCode = entry.statusCode,
                        uncompressedSize = entry.uncompressedSize,
                    )
                }
            }else {
                logger?.d(LOG_TAG, "$logPrefix Entry deleted externally:  ${request.url}")
//                if(entryAndLocks.locks.isEmpty()) {
//                    logger?.d(LOG_TAG, "$logPrefix Entry deleted externally: " +
//                            "${request.url} - has no locks, so removing from cache")
//
//                    lruMap.computeIfPresent(entryAndLocks.urlKey) { _, prev ->
//                        prev.copy(
//                            entry = null
//                        )
//                    }
//
//                    pendingCacheEntryUpserts.update { prev ->
//                        prev.filter { it.key != entryAndLocks.urlKey }
//                    }
//
//                    pendingCacheEntryDeletes.update { prev ->
//                        prev + entry
//                    }
//                }else {
//                    logger?.w(LOG_TAG, "$logPrefix Entry deleted externally: " +
//                            "${request.url} - BUT IT HAD LOCKS!!! Not good!")
//                }
            }
        }

        logger?.d(LOG_TAG, "$logPrefix MISS ${request.url}")
        return null
    }

    override suspend fun updateLastValidated(validatedEntry: ValidatedEntry) {
        val timeNow = Clock.System.now().toEpochMilliseconds()

        val url = Url(validatedEntry.url)
        memoryCache.update(url) { prevEntry ->
            val existingEntry = prevEntry.entry
            if(existingEntry != null) {
                val existingHeaders = IHttpHeaders.fromString(existingEntry.responseHeaders)

                val newHeadersCorrected = validatedEntry.headers.mapHeaders { headerName, headerValue ->
                    when {
                        NOT_MODIFIED_IGNORE_HEADERS.any { headerName.equals(it, true) } -> null
                        else -> headerValue
                    }
                }
                val newHeaders = MergedHeaders(newHeadersCorrected, existingHeaders)

                prevEntry.copy(
                    entry = existingEntry.copy(
                        responseHeaders = newHeaders.asString(),
                        lastValidated = timeNow,
                        lastAccessed = timeNow,
                    )
                )
            }else {
                prevEntry
            }
        }
    }

    override suspend fun getCacheEntry(url: String): CacheEntry? {
        assertNotClosed()
        return memoryCache.getOrLoadFromDb(Url(url)).entry?.copy()
    }

    override suspend fun getLocks(url: String): List<RetentionLock> {
        assertNotClosed()
        return memoryCache.getOrLoadFromDb(Url(url)).locks
    }

    override suspend fun getEntries(urls: Set<String>): Map<String, CacheEntry> {
        assertNotClosed()
        return db.useReaderConnection { con ->
            con.deferredTransaction {
                urls.mapNotNull { url ->
                    memoryCache.getOrLoadFromDb(Url(url)).entry?.let { entry ->
                        url to entry
                    }
                }.toMap()
            }
        }
    }

    override suspend fun getEntriesLocallyAvailable(urls: Set<String>): Map<String, Boolean> {
        assertNotClosed()
        val hashesToUrl = urls.associateBy {
            xxStringHasher.hash(it)
        }

        val availableEntryMap = mutableMapOf<String, Boolean>()


        urls.chunked(100).forEach { chunkedList ->
            val availableHashes = db.neighborCacheEntryDao.findAvailableEntries(
                chunkedList.map { xxStringHasher.hash(it) }
            )

            availableHashes.forEach { availableHash ->
                val availableUrl = hashesToUrl[availableHash]
                if(availableUrl != null) {
                    availableEntryMap[availableUrl] = true
                }else {
                    logger?.w(LOG_TAG, "Strangely could not find url in getEntriesAvailable")
                }
            }
        }


        return availableEntryMap
    }

    private fun CacheEntry.isStoredIn(parent: Path): Boolean {
        val currentPath = Path(storageUri)
        return currentPath.toString().startsWith(parent.toString())
    }

    /**
     * Used when an existing cache entry is locked or unlocked
     */
    private fun CacheEntry.moveToNewPath(destParent: Path): CacheEntry? {
        val currentPath = Path(storageUri)
        if(!fileSystem.exists(currentPath))
            return null //file with body no longer exists. Might have been deleted by OS.

        if(!fileSystem.exists(destParent)) {
            fileSystem.createDirectories(destParent)
        }

        return if(!currentPath.toString().startsWith(destParent.toString())) {
            val newDestPath = Path(destParent, currentPath.name)
            logger?.d(LOG_TAG, "$logPrefix moveToNewPath (${this.url}) $currentPath -> $newDestPath")
            fileSystem.moveWithFallback(currentPath, newDestPath)
            copy(storageUri = newDestPath.toString())
        }else {
            this
        }
    }

    override suspend fun addRetentionLocks(
        locks: List<EntryLockRequest>
    ): List<Pair<EntryLockRequest, RetentionLock>> {
        logger?.v(LOG_TAG) {
            "$logPrefix add retention locks for ${locks.joinToString { it.url } }"
        }
        assertNotClosed()
        val md5Digest = Md5Digest()

        return locks.map { lockRequest ->
            val key = md5Digest.urlKey(lockRequest.url)
            val newLock = RetentionLock(
                lockId = lockIdAtomic.incrementAndGet(),
                lockKey = key,
                lockRemark = lockRequest.remark,
                lockPublicationUid = lockRequest.publicationUid,
            )

            memoryCache.update(Url(lockRequest.url)) { prev ->
                val isNewlyLocked = prev.locks.isEmpty()

                prev.copy(
                    entry = if(isNewlyLocked) {
                        val persistentPath = pathsProvider().persistentPath
                        prev.entry?.takeIf {
                            !it.isStoredIn(persistentPath)
                        }?.moveToNewPath(persistentPath) ?: prev.entry
                    }else {
                        prev.entry
                    },
                    locks = prev.locks + newLock
                )
            }
            Pair(lockRequest, newLock)
        }
    }

    /**
     * Lock removal is done by adding it to the pending list. This isn't urgent. This avoids a large
     * number of database transactions running when lots of small files are being uploaded
     */
    override suspend fun removeRetentionLocks(locksToRemove: List<RemoveLockRequest>) {
        logger?.v(LOG_TAG) {
            "$logPrefix remove retention locks for ${locksToRemove.joinToString { "#${it.lockId}${it.url}" } }"
        }
        assertNotClosed()

        locksToRemove.forEach { removeRequest ->
            memoryCache.update(Url(removeRequest.url)) { prev ->
                val newLockList = prev.locks.filter { it.lockId != removeRequest.lockId }
                val isNewlyUnlocked = prev.locks.isNotEmpty() && newLockList.isEmpty()

                prev.copy(
                    locks = prev.locks.filter { it.lockId != removeRequest.lockId },
                    entry = if(isNewlyUnlocked) {
                        prev.entry?.moveToNewPath(pathsProvider().cachePath)
                    }else {
                        prev.entry
                    }
                )
            }
        }
    }

    override suspend fun findLocksByPublicationUid(publicationUid: Long): List<RetentionLock> {
        assertNotClosed()
        return db.retentionLockDao.findByPublicationUid(publicationUid)
    }

    override suspend fun pinPublication(manifestUrl: Url) {
        assertNotClosed()
        enqueuePinPublicationPrepareUseCase(manifestUrl)
    }

    override suspend fun unpinPublication(manifestUrl: Url) {
        assertNotClosed()
        val locks = findLocksByPublicationUid(
            xxStringHasher.hash(manifestUrl.toString())
        )

        removeRetentionLocks(
            locks.map {
                RemoveLockRequest(
                    url = it.lockKey,
                    lockId = it.lockId
                )
            }
        )

        db.downloadJobDao.updateStatusByManifestHash(
            manifestHash = xxStringHasher.hash(manifestUrl.toString()),
            status = TransferJobItemStatus.STATUS_CANCELLED
        )

        //Do nothing yet
    }

    override fun publicationPinState(manifestUrl: Url): Flow<PublicationPinState> {
        assertNotClosed()
        return db.downloadJobItemDao.publicationPinState(
            pubManifestHash = xxStringHasher.hash(manifestUrl.toString())
        )
    }

    override suspend fun setExtraResponseHeaders(
        url: Url,
        extraResponseHeaders: Headers
    ) {
        assertNotClosed()
        memoryCache.update(url) { prev ->
            prev.copy(
                extraHeaders = CacheEntryExtraHeaders(
                    ceehKey = prev.urlKey,
                    ceehUrl = url.toString(),
                    extraHeaders = extraResponseHeaders.asRawString()
                )
            )
        }
    }

    private fun assertNotClosed() {
        if(closed.value) {
            val exception = IllegalStateException("$logPrefix: Cache is closed")
            logger?.e(LOG_TAG, "Closed", throwable = exception)
            throw exception
        }
    }

    override fun close() {
        if(!closed.getAndUpdate { true }) {
            logger?.i(LOG_TAG, "$logPrefix: closing")
            scope.cancel()
            updatesToCommitChannel.close()
            logger?.i(LOG_TAG, "$logPrefix: closed")
        }else {
            logger?.i(LOG_TAG, "$logPrefix: already closed")
        }
    }

    companion object {
        const val LOG_TAG = "UstadCache"

        /**
         * When an entry is validated, most headers will be updated with those found on the 304
         * not modified response e.g. Age, Cache-Control, Last-Modified etc.
         *
         * All values on the 304 not-modified SHOULD be the same as would otherwise be returned, however:
         *  1) KTOR, and other servers, use content-length: 0 (a 304 not-modified response has no
         *     body, so that response has a length of zero, but strictly speaking, this is wrong)
         *  2) content-encoding : this could be changed internally (e.g. by updating what mime types
         *     are or are not compressed). When a 304 response is received, the response body stored
         *     on disk is not changed, so the content-encoding must NEVER change.
         */
        private val NOT_MODIFIED_IGNORE_HEADERS = listOf("content-length", "content-encoding")

        /**
         * The number of entries to hold in the in-memory cache. Important: this does NOT hold the
         * body of the response; only the headers and metadata
         */
        const val MEMORY_CACHE_DEFAULT_NUM_ENTRIES = 10_000L

        private const val COMMIT_RETRY_DELAY = 500

    }
}