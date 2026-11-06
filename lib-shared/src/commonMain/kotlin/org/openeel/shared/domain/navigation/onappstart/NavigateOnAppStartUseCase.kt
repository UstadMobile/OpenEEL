package org.openeel.shared.domain.navigation.onappstart

import com.russhwolf.settings.Settings
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.domain.navigation.deeplink.CustomDeepLinkToUrlUseCase
import org.openeel.shared.domain.navigation.deeplink.InitDeepLinkUriProviderUseCase
import org.openeel.shared.domain.navigation.deferreddeeplink.GetDeferredDeepLinkUseCase
import org.openeel.shared.domain.urltonavcommand.ResolveUrlToNavCommandUseCase
import org.openeel.shared.ext.withClearBackstack
import org.openeel.shared.navigation.AssignmentList
import org.openeel.shared.navigation.GetStartedScreen
import org.openeel.shared.navigation.Home
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.LoginSelectClass
import kotlin.time.Duration.Companion.milliseconds

/**
 * Decide where to navigate to when the app is starting. The logic is roughly:
 * - If there is any deep link to use (Deferred Deep Link or cold start deep link) then go there
 *   using resolveUrlToNavCommandUseCase
 * - If user already has account signed in: Go to AssignmentList if it is a simplified child mode
 *   account, otherwise go to AppLauncher
 *
 * This is used in two places:
 *
 * a) AcknowledgementViewModel: the first screen of the app. If the GetStarted screen has already
 *    been shown, then it will use this use case to determine where to navigate to
 * b) GetStartedViewModel: will always use this use case to determine where to navigate to after the
 *    user clicks the get started button.
 * c) [org.openeel.shared.viewmodel.manageuser.waitingforapproval.WaitingForApprovalViewModel] to
 *    navigate once a user's new account has been approved.
 */
class NavigateOnAppStartUseCase(
    private val accountManager: AppAccountManager,
    private val initDeepLinkUriProvider: InitDeepLinkUriProviderUseCase,
    val getDeferredDeepLinkUseCase: GetDeferredDeepLinkUseCase?,
    private val customDeepLinkToUrlUseCase: CustomDeepLinkToUrlUseCase,
    private val resolveUrlToNavCommandUseCase: ResolveUrlToNavCommandUseCase,
    private val settings: Settings
) {

    /**
     * @param useDeepLinks if true (by default) check using [GetDeferredDeepLinkUseCase] and
     *        [InitDeepLinkUriProviderUseCase] for a deep link to use. If we are starting fresh for
     *        another reason (e.g. the user just activated shared device mode) then this is set to
     *        false to avoid unexpected re-use of deep links.
     */
    suspend operator fun invoke(
        useDeepLinks: Boolean = true,
    ): NavCommand {
        val getDeferredDeepLinkUseCaseVal = getDeferredDeepLinkUseCase

        val deferredDeepLink = if(
            useDeepLinks &&
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
            Napier.i(
                "DeferredDeepLink: NavigateOnAppStartUseCase : useDeepLinks=false or no use case available"
            )
            null
        }

        Napier.i("DeferredDeepLink: NavigateOnAppStartUseCase creating initLinkNavCommand: deferredDeepLink=$deferredDeepLink")
        val initLinkNavCommand: NavCommand? = try {
            (deferredDeepLink ?: initDeepLinkUriProvider.takeIf {
                useDeepLinks
            }?.invoke())?.let { initDeepLink ->
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
        val activeAccount = accountManager.activeAccount
        val selectedAccountAndPerson = accountManager.selectedAccountAndPersonFlow.first()
        val isSimplifiedChildMode = selectedAccountAndPerson?.isSimplifiedChildMode == true

        return initLinkNavCommand?.withClearBackstack(true) ?: NavCommand.Navigate(
            destination = when {
                selectedAccountAndPerson?.isSharedDeviceSessionWithoutStudentSelected == true -> {
                    LoginSelectClass
                }


                activeAccount != null -> {
                    if (isSimplifiedChildMode) AssignmentList else Home
                }

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