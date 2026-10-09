package world.respect.shared.domain.navigation.deeplink

import android.content.Intent
import android.net.Uri
import java.util.concurrent.atomic.AtomicReference
import androidx.core.net.toUri

/**
 * Implementation of InitDeepLinkUriProviderUseCase where the Activity can call onSetDeepLink from
 * its onCreate function. The initial deep link can then be retrieved by the
 * AcknowledgementViewModel which can use it to direct the user to the appropriate deep link if
 * required.
 *
 * A named bundle argument can be used as an alternative (e.g. when running Maestro flows locally).
 */
class InitDeepLinkUriProviderUseCaseAndroid(): InitDeepLinkUriProviderUseCase {

    private val initDeepLinkUri = AtomicReference<Uri?>(null)

    fun onSetDeepLink(
        intent: Intent
    ) {
        initDeepLinkUri.set(intent.data ?: intent.getStringExtra(BUNDLE_ARG_NAME)?.toUri())
    }

    override fun invoke(): String? {
        return initDeepLinkUri.get()?.toString()
    }

    companion object {

        const val BUNDLE_ARG_NAME = "launchUrl"

    }
}