package world.respect.shared.domain.account.username.checkusernameunique

/**
 * Checks that the given username is unique for the school.
 *
 * Requires a connection to the server. Server implementation uses the database. Client
 * implementation makes an HTTP call.
 */
interface CheckUsernameUniqueUseCase {

    /**
     * Check if the given username is unique e.g. does not already exist.
     *
     * @return true if the username is unique, false otherwise
     */
    suspend operator fun invoke(username: String): Boolean

    companion object {

        const val ENDPOINT_NAME = "checkusernameunique"

        const val ENDPOINT_PATH = "username/$ENDPOINT_NAME"

        const val PARAM_USERNAME = "username"

    }
}