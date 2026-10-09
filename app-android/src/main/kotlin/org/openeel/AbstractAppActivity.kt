package world.respect.view.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.ustadmobile.libuicompose.theme.RespectAppTheme
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.koin.android.ext.android.inject
import world.respect.app.app.App
import world.respect.app.app.SizeClass
import world.respect.shared.domain.navigation.deeplink.CustomDeepLinkToUrlUseCase
import world.respect.shared.domain.navigation.deeplink.InitDeepLinkUriProviderUseCaseAndroid
import world.respect.shared.domain.urltonavcommand.ResolveUrlToNavCommandUseCase
import world.respect.shared.ext.withClearBackstack
import world.respect.shared.navigation.NavCommand


abstract class AbstractAppActivity : AppCompatActivity() {

    private val customDeepLinkToUrlUseCase: CustomDeepLinkToUrlUseCase by inject()

    private val resolveUrlToNavCommandUseCase: ResolveUrlToNavCommandUseCase by inject()

    private val initDeepLinkUriProviderAndroid: InitDeepLinkUriProviderUseCaseAndroid by inject()

    val WindowWidthSizeClass.multiplatformSizeClass: SizeClass
        get() = when (this) {
            WindowWidthSizeClass.Compact -> SizeClass.COMPACT
            WindowWidthSizeClass.Medium -> SizeClass.MEDIUM
            WindowWidthSizeClass.Expanded -> SizeClass.EXPANDED
            else -> SizeClass.MEDIUM
        }

    private val _navCommandFlow = MutableSharedFlow<NavCommand>(
        replay = 1,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val navCommandFlow: SharedFlow<NavCommand> = _navCommandFlow.asSharedFlow()

    @OptIn(
        ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
        ExperimentalMaterial3WindowSizeClassApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initDeepLinkUriProviderAndroid.onSetDeepLink(intent)

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)

            RespectAppTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics {
                            testTagsAsResourceId = true
                        },
                    color = MaterialTheme.colorScheme.background
                ) {
                    App(
                        widthClass = windowSizeClass.widthSizeClass.multiplatformSizeClass,
                        activityNavCommandFlow = navCommandFlow,
                    )
                }
            }
        }
    }

    /**
     * Activity uses singleTop mode - so a new intent will not trigger destruction/recreation
     *
     * We don't want this because the RespectAccountManager is modelled as using a single account at
     * a time.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val intentUri = intent.data ?: return

        try {
            val url = customDeepLinkToUrlUseCase(Url(intentUri.toString()))
            resolveUrlToNavCommandUseCase(url, canGoBack = false)
                ?.withClearBackstack(true)?.also {
                    _navCommandFlow.tryEmit(it)
                }
        }catch(e: Throwable) {
            Napier.w("Exception handling link", e)
        }
    }
}
