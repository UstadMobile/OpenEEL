package com.ustadmobile.libcache.okhttp

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.ustadmobile.libcache.CachePaths
import com.ustadmobile.libcache.CachePathsProvider
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheImpl
import com.ustadmobile.libcache.cachecontrol.CacheControlFreshnessCheckerImpl
import com.ustadmobile.libcache.connectivitymonitor.ConnectivityMonitorJvm
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.downloader.EnqueuePinPublicationPrepareUseCaseJvm
import com.ustadmobile.libcache.logging.NapierLoggingAdapter
import com.ustadmobile.libcache.util.initNapierLog
import io.ktor.client.HttpClient
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.openeel.libxxhash.jvmimpl.XXStringHasherCommonJvm
import java.io.File
import java.time.Duration
import kotlin.test.BeforeTest
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.AfterTest

/**
 * Test that contains common setup for any test that uses the OKHttp interceptor e.g. The
 * UstadCacheInterceptTest itself and DownloadIntegrationTest
 */
abstract class AbstractCacheInterceptorTest {

    @JvmField
    @Rule
    val tempDir = TemporaryFolder()

    protected lateinit var okHttpClient: OkHttpClient

    protected lateinit var cacheRootDir: File

    protected lateinit var cachePathsProvider: CachePathsProvider

    protected lateinit var interceptorTmpDir: File

    protected lateinit var cacheDb: UstadCacheDb

    protected lateinit var ustadCache: UstadCacheImpl

    protected lateinit var cacheListener: UstadCache.CacheListener

    protected lateinit var httpClient: HttpClient

    protected val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }


    @BeforeTest
    fun setup() {
        initNapierLog()
        val logger = NapierLoggingAdapter()
        cacheListener = mock { }
        cacheRootDir = tempDir.newFolder("cachedir")
        cachePathsProvider = CachePathsProvider {
            Path(cacheRootDir.absolutePath).let {
                CachePaths(
                    tmpWorkPath = Path(it, "tmpWork"),
                    persistentPath = Path(it, "persistent"),
                    cachePath = Path(it, "cache"),
                )
            }
        }
        interceptorTmpDir = tempDir.newFolder("interceptor-tmp")
        cacheDb = Room.databaseBuilder<UstadCacheDb>(
            tempDir.newFile("ustadcache.db").absolutePath
        ).setDriver(BundledSQLiteDriver()).build()
        ustadCache = spy(
            UstadCacheImpl(
                pathsProvider = cachePathsProvider,
                db = cacheDb,
                logger = logger,
                listener = cacheListener,
                xxStringHasher = XXStringHasherCommonJvm(),
                enqueuePinPublicationPrepareUseCase = EnqueuePinPublicationPrepareUseCaseJvm(
                    cacheDb, XXStringHasherCommonJvm()
                ),
                freshnessChecker = CacheControlFreshnessCheckerImpl(),
            )
        )
        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(
                UstadCacheInterceptor(
                    ustadCache, { interceptorTmpDir } ,
                    logger = logger,
                    json = json,
                    connectivityMonitor = ConnectivityMonitorJvm(),
                ))
            .callTimeout(Duration.ofSeconds(500))
            .connectTimeout(Duration.ofSeconds(500))
            .readTimeout(Duration.ofSeconds(500))
            .build()

        httpClient = HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(json = json)
            }

            engine {
                preconfigured = okHttpClient
            }
        }
    }

    @AfterTest
    fun tearDown() {
        ustadCache.close()

        //See UstadCacheJvmTest note
        //cacheDb.close()

        httpClient.close()
    }

}