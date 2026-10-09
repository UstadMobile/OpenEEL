package world.respect.datalayer

import world.respect.datalayer.school.model.AuthToken

/**
 * Interface that will provide an auth token for a specific account. This is handled in the
 * dependency injection as a provider, not an object, because the token might be refreshed during
 * the lifetime of the app / components.
 *
 * Using a provider allows the token to be refreshed without needing to close datasources etc.
 */
fun interface AuthTokenProvider {

    fun provideToken(): AuthToken

}