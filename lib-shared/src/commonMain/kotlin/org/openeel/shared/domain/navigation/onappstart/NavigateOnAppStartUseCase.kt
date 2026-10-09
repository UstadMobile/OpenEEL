package world.respect.shared.domain.navigation.onappstart

import com.russhwolf.settings.Settings
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.navigation.deeplink.CustomDeepLinkToUrlUseCase
import world.respect.shared.domain.navigation.deeplink.InitDeepLinkUriProviderUseCase
import world.respect.shared.domain.navigation.deferreddeeplink.GetDeferredDeepLinkUseCase
import world.respect.shared.domain.urltonavcommand.ResolveUrlToNavCommandUseCase
import world.respect.shared.ext.withClearBackstack
import world.respect.shared.navigation.AssignmentList
import world.respect.shared.navigation.GetStartedScreen
import world.respect.shared.navigation.Home
import world.respect.shared.navigation.NavCommand
import kotlin.time.Duration.Companion.milliseconds

/**
 * Decide where to navigate to when the app is starting. The logic is roughly:
 * - If there is any deep link to use (Deferred Deep Link or cold start deep link) then go there
 *   using resolveUrlToNavCommandUseCase
 * - If user already has account signed in: Go to AssignmentList if it is a simplified child mode
 *   account, otherwise go to RespectAppLauncher
 *
 * This is used in two places:
 *
 * a) AcknowledgementViewModel: the first screen of the app. If the GetStarted screen has already
 *    been shown, then it will use this use case to determine where to navigate to
 * b) GetStartedViewModel: will always use this use case to determine where to navigate to after the
 *    user clicks the get started button.
 */
class NavigateOnAppStartUseCase(
    private val accountManager: RespectAccountManager,
    private val initDeepLinkUriProvider: InitDeepLinkUriProviderUseCase,
    val getDeferredDeepLinkUseCase: GetDeferredDeepLinkUseCase?,
    private val customDeepLinkToUrlUseCase: CustomDeepLinkToUrlUseCase,
    private val resolveUrlToNavCommandUseCase: ResolveUrlToNavCommandUseCase,
    private val settings: Settings
) {

    suspend operator fun invoke(): NavCommand {
        val getDeferredDeepLinkUseCaseVal = getDeferredDeepLinkUseCase

        val deferredDeepLink = if(
            getDeferredDeepLinkUseCaseVal != null &&
            !settings.getBoolean(KEY_DEFERRED_DEEP_LINK_CHECK_DONE, false)
        ) {
            Napier.i("DeferredDeepLink: NavigateOnAppStartUseCase checking for deferred deep link")
            withTimeoutOrNull(GET_DEFERRED_LINK_TIMEOUT.milliseconds) {
                getDeferredDeepLinkUseCaseVal().also { deferredDeepLink ->
                    Napier.i("DeferredDeepLink: NavigateOnAppStartUseCase invoked: deferredDeepLink=$deferredDeepLink")
                    settings.putBoolean(KEY_DEFERRED_DEEP_LINK_CHECK_DONE, true)
                }
            }
        }else{
            Napier.i("DeferredDeepLink: NavigateOnAppStartUseCase : no use case available")
            null
        }

        Napier.i("DeferredDeepLink: NavigateOnAppStartUseCase creating initLinkNavCommand: deferredDeepLink=$deferredDeepLink")
        val initLinkNavCommand: NavCommand? = try {
            (deferredDeepLink ?: initDeepLinkUriProvider())?.let { initDeepLink ->
                resolveUrlToNavCommandUseCase(
                    url = customDeepLinkToUrlUseCase(Url(initDeepLink)),
                    canGoBack = false,
                )
            }
        }catch(e: Throwable){
            Napier.w("Exception handling initial link", e)
            null
        }

        Napier.i("DeferredDeepLink: NavigateOnAppStartUseCase: init nav command=$initLinkNavCommand")
        val isChild = accountManager.selectedAccountAndPersonFlow.first()?.isChild == true
        val hasAccount = accountManager.activeAccount != null


        return initLinkNavCommand?.withClearBackstack(true) ?: NavCommand.Navigate(
            destination = when {
                hasAccount -> if (isChild) AssignmentList else Home
                else -> GetStartedScreen()
            },
            clearBackStack = true,
        )
    }

    companion object {

        private const val GET_DEFERRED_LINK_TIMEOUT = 1_000L

        const val KEY_DEFERRED_DEEP_LINK_CHECK_DONE = "deferredDeepLinkCheckDone"

    }
}