package org.openeel.lib.directory.keycloak

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.encodedPath
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.openeel.datalayer.AuthTokenProvider
import org.openeel.datalayer.respect.model.KeycloakUserDirectoryConfig
import org.openeel.lib.dataloadstate.DataErrorResult
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.lib.dataloadstate.throwable.HttpErrorResponseException
import org.openeel.lib.directory.model.Person
import org.openeel.lib.directory.resources.PersonDirectoryResource

class KeycloakPersonDirectoryResource(
    config: KeycloakUserDirectoryConfig,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
) : PersonDirectoryResource {

    private val adminEndpoint = URLBuilder(config.endpoint).apply {
        val realm = encodedPath.substringAfterLast(REALM_PATH, "")
        encodedPath = encodedPath.removeSuffix("$REALM_PATH$realm") + "$ADMIN_REALM_PATH$realm"
    }.build()

    override suspend fun list(
        loadParams: DataLoadParams,
        params: PersonDirectoryResource.GetListParams,
    ): DataLoadState<List<Person>> {
        if (loadParams.onlyIfCached) return NoDataLoadedState.notFound()
        return try {
            val query = buildMap {
                put(MAX_PARAM, USER_LIST_LIMIT.toString())
                params.filterByName?.let { put(SEARCH_PARAM, it) }
            }
            val users = get<List<KeycloakUser>>(KEYCLOAK_USERS_PATH, query = query)
            DataReadyState(users.map { it.toPerson() })
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            DataErrorResult(exception)
        }
    }

    override fun listAsFlow(
        loadParams: DataLoadParams,
        params: PersonDirectoryResource.GetListParams,
    ): Flow<DataLoadState<List<Person>>> = flow { emit(list(loadParams, params)) }

    suspend fun findByGuid(guid: String): Person =
        get<KeycloakUser>(KEYCLOAK_USERS_PATH, guid).toPerson()

    private suspend inline fun <reified T> get(
        vararg path: String,
        query: Map<String, String> = emptyMap(),
    ): T {
        val url = URLBuilder(adminEndpoint).apply {
            appendPathSegments(*path)
            query.forEach { (key, value) -> parameters.append(key, value) }
        }.build()

        val response = httpClient.get(url) {
            bearerAuth(tokenProvider.provideToken().accessToken)
        }

        if (!response.status.isSuccess()) {
            throw HttpErrorResponseException(
                response.status.value,
                "Unable to load Keycloak users"
            )
        }

        return response.body()
    }

    override suspend fun store(list: List<Person>) {
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

