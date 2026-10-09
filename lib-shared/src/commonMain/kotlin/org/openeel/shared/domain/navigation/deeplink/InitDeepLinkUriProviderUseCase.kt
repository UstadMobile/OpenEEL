package world.respect.shared.domain.navigation.deeplink


/**
 * When the app starts it may, or may not, have an initial deep link to open. On Android this has to
 * be received using the Activity's intent.
 */
interface InitDeepLinkUriProviderUseCase {

    operator fun invoke(): String?

}