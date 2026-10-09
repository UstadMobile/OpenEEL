package world.respect.shared.domain.navigation.deferreddeeplink

/**
 * Get a deferred deep link: If a user opens a link that was shared with them, but they don't yet
 * have the app installed, we use a deferred deep link approach as follows to enable them to
 * continue seamlessly once they install the app:
 * a) The http response for the link URL redirects user to app store (e.g. Google Play) with a
 *    referrer parameter set.
 * b) When the user first opens the app the referrer parameter is retrieved. If set, then it is used
 *    to navigate to the deep link.
 *
 * On Android this is done using the Play Install Referrer Library:
 * https://developer.android.com/google/play/installreferrer/library
 */
interface GetDeferredDeepLinkUseCase {

    suspend operator fun invoke(): String?

    companion object {

        const val PARAM_NAME_DEFERRED_DEEP_LINK = "deferredDeepLink"

    }


}