package org.openeel.lib.directory.keycloak

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.appendPathSegments
import io.ktor.http.encodedPath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.openeel.datalayer.AuthTokenProvider
import org.openeel.datalayer.ext.getAsDataLoadState
import org.openeel.datalayer.ext.getDataLoadResultAsFlow
import org.openeel.datalayer.respect.model.KeycloakUserDirectoryConfig
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.ext.map
import org.openeel.lib.directory.keycloak.adapters.toPerson
import org.openeel.lib.directory.keycloak.model.KeycloakUserRepresentation
import org.openeel.lib.directory.model.Person
import org.openeel.lib.directory.resources.PersonDirectoryResource
import kotlin.collections.orEmpty

class KeycloakPersonDirectoryResource(
    val config: KeycloakUserDirectoryConfig,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
) : PersonDirectoryResource {

    private val adminEndpoint = URLBuilder(config.endpoint).apply {
        val realm = encodedPath.substringAfterLast(REALM_PATH, "")
        encodedPath = encodedPath.removeSuffix("$REALM_PATH$realm") + "$ADMIN_REALM_PATH$realm"
    }.build()

    private fun PersonDirectoryResource.GetListParams.urlWithParams(): Url {
        val personGuid = filterByPersonGuid
        return URLBuilder(adminEndpoint).apply {
            if (personGuid != null) {
                appendPathSegments(KEYCLOAK_USERS_PATH, personGuid)
            } else {
                // Docs: https://www.keycloak.org/docs-api/latest/rest-api/index.html
                // See GET /admin/realms/{realm}/users
                appendPathSegments(KEYCLOAK_USERS_PATH)
                filterByName?.let { parameters.append(SEARCH_PARAM, it) }
            }
            parameters.append(MAX_PARAM, USER_LIST_LIMIT.toString())
        }.build()
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        params: PersonDirectoryResource.GetListParams,
    ): DataLoadState<List<Person>> {
        return httpClient.getAsDataLoadState<List<KeycloakUserRepresentation>>(
            url = params.urlWithParams(),
        ) {
            bearerAuth(tokenProvider.provideToken().accessToken)
        }.map { users ->
            val filteredUsers = filters(
                params = params,
                users = users
            )
            filteredUsers.map { it.toPerson() }
        }
    }

    override fun listAsFlow(
        loadParams: DataLoadParams,
        params: PersonDirectoryResource.GetListParams,
    ): Flow<DataLoadState<List<Person>>> {
        if (params.filterByPersonGuid != null) {
           return httpClient.getDataLoadResultAsFlow<KeycloakUserRepresentation>(
                urlFn = { params.urlWithParams() },
                dataLoadParams = loadParams,
            ) {
                bearerAuth(tokenProvider.provideToken().accessToken)
            }.map {
                state -> state.map { user ->
                    filters(
                       params = params,
                       users =  listOf(user)
                    ).map { it.toPerson() }
                }
            }
        }
        return httpClient.getDataLoadResultAsFlow<List<KeycloakUserRepresentation>>(
            urlFn = { params.urlWithParams() },
            dataLoadParams = loadParams,
            block = {
                bearerAuth(tokenProvider.provideToken().accessToken)
            },
        ).map { dataLoadState ->
            dataLoadState.map { users ->
                filters(
                    params = params,
                    users =  users
                ).map { it.toPerson() }            }
        }
    }


    suspend fun findByGuid(guid: String): DataLoadState<Person> {
        return httpClient.getAsDataLoadState(
            url = URLBuilder(adminEndpoint).apply {
                appendPathSegments(KEYCLOAK_USERS_PATH, guid)
            }.build(),
            bodyAdapter = { response -> response.body<KeycloakUserRepresentation>().toPerson() },
        ) {
            bearerAuth(tokenProvider.provideToken().accessToken)
        }
    }

    override suspend fun store(list: List<Person>) {
    }

    private fun filters(
        params: PersonDirectoryResource.GetListParams,
        users: List<KeycloakUserRepresentation>,
    ): List<KeycloakUserRepresentation> {
        val roleName = params.filterByPersonRole?.value ?: return users
        return users.filter { roleName in it.realmRoles.orEmpty() }
    }

    companion object {
        const val KEYCLOAK_USERS_PATH = "users"
        private const val REALM_PATH = "/realms/"
        private const val ADMIN_REALM_PATH = "/admin/realms/"
        private const val MAX_PARAM = "max"
        private const val SEARCH_PARAM = "search"
        private const val USER_LIST_LIMIT = 100
    }
}

