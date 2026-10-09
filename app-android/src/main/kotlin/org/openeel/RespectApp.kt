package world.respect

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import okhttp3.OkHttpClient
import org.acra.config.httpSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.acra.sender.HttpSender
import org.koin.android.ext.android.get
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import world.respect.app.BuildConfig
import world.respect.lib.xapi.XapiResourceProvider
import world.respect.lib.xapi.resources.XapiResource
import org.openeel.libcache.ipc.server.OkHttpClientProvider

class RespectApp : Application(), SingletonImageLoader.Factory, XapiResourceProvider, OkHttpClientProvider {


    override fun onCreate() {
        super.onCreate()
        Napier.base(DebugAntilog())

        //See https://stackoverflow.com/questions/23844667/how-do-i-detect-if-i-am-in-release-or-debug-mode
        /* uncomment if needed for web content debugging
        if(applicationInfo.flags.and(FLAG_DEBUGGABLE) == FLAG_DEBUGGABLE) {
            //Debugging enabled in webview is false by default. Have seen crash reports caused by
            // this line; which is not needed in release.
            WebView.setWebContentsDebuggingEnabled(true)
        }
        */

        startKoin {
            androidContext(this@RespectApp)
            modules(appKoinModule)
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        if(BuildConfig.ACRA_URI.isNotEmpty()) {
            initAcra {
                reportFormat = StringFormat.JSON
                httpSender {
                    uri = BuildConfig.ACRA_URI
                    basicAuthLogin = BuildConfig.ACRA_BASICAUTHLOGIN.trim()
                    basicAuthPassword = BuildConfig.ACRA_BASICAUTHPASSWORD.trim()
                    httpMethod = HttpSender.Method.POST
                }
            }
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        val okHttpClient: OkHttpClient = get()
        return ImageLoader.Builder(applicationContext)
            .components {
                //As per https://coil-kt.github.io/coil/network/#using-a-custom-okhttpclient
                add(OkHttpNetworkFetcherFactory(callFactory = { okHttpClient } ))
            }
            .crossfade(true)
            .build()
    }

    override suspend fun provideXapiResource(
        endpoint: Url,
        authentication: String?
    ): XapiResource {
        return getKoin().get<XapiResourceProvider>().provideXapiResource(endpoint, authentication)
    }

    override fun provideOkHttpClient(): OkHttpClient {
        return getKoin().get()
    }

}
