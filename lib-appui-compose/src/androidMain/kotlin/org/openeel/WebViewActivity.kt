package world.respect

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.ustadmobile.libcache.webview.OkHttpWebViewClient
import io.github.aakira.napier.Napier
import okhttp3.OkHttpClient
import org.koin.android.ext.android.inject
import world.respect.appcompose.R
import world.respect.shared.domain.launchapp.LaunchAppUseCaseAndroid

/**
 * A separate activity that only shows a WebView (e.g. to view a LearningUnit) .
 *
 * This can't be done as normal Jetpack Compose using the AndroidView as normal because the vh css
 * unit doesn't work; content that uses 100vh etc comes out as zero height or a small percentage of
 * the screen (at random).
 */
class WebViewActivity : AppCompatActivity() {

    private val webChromeClient = object: WebChromeClient() {

        private fun ConsoleMessage.toLogLine(): String {
            return buildString {
                message()?.also { append(it) }
                append(" lineNum=${lineNumber()}")
                sourceId()?.also {
                    append(" sourceId=$it")
                }
            }
        }

        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
            val messageLevel = consoleMessage?.messageLevel() ?: return false

            when(messageLevel) {
                ConsoleMessage.MessageLevel.LOG -> Log.i(LOGTAG, consoleMessage.toLogLine())
                ConsoleMessage.MessageLevel.WARNING -> Log.w(LOGTAG, consoleMessage.toLogLine())
                ConsoleMessage.MessageLevel.ERROR -> Log.e(LOGTAG, consoleMessage.toLogLine())
                ConsoleMessage.MessageLevel.TIP -> Log.i(LOGTAG, consoleMessage.toLogLine())
                ConsoleMessage.MessageLevel.DEBUG -> Log.d(LOGTAG, consoleMessage.toLogLine())
            }

            return true
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)
            this@WebViewActivity.title = title ?: ""
        }

        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            val progressBar = findViewById<ProgressBar>(R.id.progress_bar) ?: return

            if(newProgress < 100) {
                progressBar.progress = newProgress
                progressBar.takeIf { it.visibility != View.VISIBLE }?.visibility = View.VISIBLE
            }else {
                progressBar.takeIf { it.visibility != View.GONE }?.visibility = View.GONE
            }
        }
    }

//    private val httpIpcClient by lazy {
//        HttpIpcClientBuilder(this)
//            .setIpcServicePackageName(this.packageName)
//            .build()
//    }

    private val okHttpClient: OkHttpClient by inject()

    //Uncomment below to test sending requests through Http-Ipc
//    private val okHttpClient: OkHttpClient by lazy {
//        val appOkHttpClient: OkHttpClient by inject<OkHttpClient>()
//        val builder = appOkHttpClient.newBuilder()
//
//        //Uncomment to test using Http-Ipc
//        builder.interceptors().add(0, HttpIpcInterceptor(httpIpcClient))
//        builder.build()
//    }

    private val webViewClient by lazy {
        OkHttpWebViewClient(okHttpClient = okHttpClient)
    }


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intentUrlExtra = intent.getStringExtra(LaunchAppUseCaseAndroid.EXTRA_URL)
        Napier.d(tag = LOGTAG, message = "WebViewActivity: onCreate: url=$intentUrlExtra")

        setContentView(R.layout.activity_web_view)

        setSupportActionBar(findViewById(R.id.toolbar))
        val webView: WebView = findViewById(R.id.web_view)
        webView.webChromeClient = webChromeClient
        webView.webViewClient = webViewClient
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false

        //Content will be loaded from HTTPs and will then make requests to 127.0.0.1 for xAPI
        //statement submission
        webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        val url = intentUrlExtra ?: throw IllegalStateException("No url specified")

        Napier.d(tag = LOGTAG, message = "WebViewActivity: onCreate:loading url=$url")
        webView.loadUrl(url)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        val webView: WebView = findViewById(R.id.web_view)
        if(webView.canGoBack()) {
            webView.goBack()
            return true
        }else {
            finish()
            return true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_webview, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.webview_close -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        //Uncomment to test using HttpIpc
//        httpIpcClient.close()
    }

    companion object {

        const val LOGTAG = "WebViewActivityTag"
    }
}