package com.ustadmobile.libcache

import androidx.room.Room
import com.ustadmobile.libcache.UstadCache.Companion.DEFAULT_SIZE_LIMIT
import com.ustadmobile.libcache.cachecontrol.CacheControlFreshnessCheckerImpl
import com.ustadmobile.libcache.db.AddNewEntryTriggerCallback
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.downloader.EnqueuePinPublicationPrepareUseCaseJvm
import com.ustadmobile.libcache.logging.UstadCacheLogger

import kotlinx.io.files.Path
import org.openeel.libxxhash.XXStringHasher
import org.openeel.libxxhash.jvmimpl.XXStringHasherCommonJvm
import java.io.File

/**
 * @param dbFile File where SQLite database is saved
 * @param storagePath the path where cache data will be stored
 * @param logger logging adapter (optional)
 * @param cacheName name (used in logging)
 * @param distributedCacheEnabled if true, then add triggers for distributed caching
 */
@Suppress("MemberVisibilityCanBePrivate")
class UstadCacheBuilder(
    var dbFile: File,
    var storagePath: Path,
    var xxStringHasher: XXStringHasher = XXStringHasherCommonJvm(),
    var logger: UstadCacheLogger? = null,
    var cacheName: String = "",
    var sizeLimit: () -> Long = { DEFAULT_SIZE_LIMIT },
    var distributedCacheEnabled: Boolean = false,
    var pathsProvider: CachePathsProvider = CachePathsProvider {
        CachePaths(
            tmpWorkPath = Path(storagePath, "tmpWork"),
            persistentPath = Path(storagePath, "persistent"),
            cachePath = Path(storagePath, "cache")
        )
    },
    var db: UstadCacheDb? = null,
){

    fun build(): UstadCache {
        val dbVal = db ?: Room.databaseBuilder<UstadCacheDb>(dbFile.absolutePath)
            .apply {
                if(distributedCacheEnabled) {
                    addCallback(AddNewEntryTriggerCallback())
                }
            }
            .build()
        return UstadCacheImpl(
            pathsProvider = pathsProvider,
            db = dbVal,
            sizeLimit = sizeLimit,
            logger = logger,
            cacheName = cacheName,
            xxStringHasher = xxStringHasher,
            enqueuePinPublicationPrepareUseCase = EnqueuePinPublicationPrepareUseCaseJvm(
                dbVal, xxStringHasher
            ),
            freshnessChecker = CacheControlFreshnessCheckerImpl(),
        )
    }
}