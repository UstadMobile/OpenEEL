package org.openeel.app.userdirectory.resource

import org.openeel.app.userdirectory.model.Person2
import org.openeel.lib.dataloadstate.DataLoadParams

/**
 * Interface that provides a directory of Persons and provides information on available permissions.
 * The local implementation will be provided by the database. The remote implementation can
 * connect to the builtin server directory or an externally managed directory (e.g. Keycloak,
 * Google, etc).
 */
interface PersonDirectoryResource {

    data class GetListParams(
        val name: String? = null,
    )

    suspend fun list(
        loadParams: DataLoadParams,
        listParams: GetListParams,
    ): List<Person2>

    /**
     * Check if the active user can manage account, create a new account, etc.
     */
    suspend fun hasPermission(
        permissions: List<Int>,
        person2: Person2,
    ): List<Boolean>

    suspend fun store(person2: Person2)

}