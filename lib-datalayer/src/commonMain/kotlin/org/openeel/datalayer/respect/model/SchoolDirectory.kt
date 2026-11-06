package org.openeel.datalayer.respect.model

import io.ktor.http.Url
import kotlinx.serialization.Serializable

/**
 * A RESPECT school directory is a base URL that:
 * Implements RESPECT directory APIs (e.g. invite management) for the given invitePrefix
 * Provides a list of RESPECT schools
 *
 * @property invitePrefix the prefix for invites that are managed by this directory
 * @property baseUrl the URL of this directory server e.g. https://respect.example.org/api/respect-directory/
 *           On the server side, there is always one realm with the baseUrl set as
 *           SERVER_MANAGED_DIRECTORY_URL.
 * @property icon optional icon URL for this directory (e.g. as provided in the host list JSON).
 *           Not persisted in the local database.
 */
@Serializable
data class SchoolDirectory(
    val invitePrefix: String,
    val baseUrl: Url,
    val name: String? = null,
    val icon: Url? = null,
) {

    companion object {

        const val SERVER_MANAGED_DIRECTORY_URL = "local-dir://localhost/"

    }

}
