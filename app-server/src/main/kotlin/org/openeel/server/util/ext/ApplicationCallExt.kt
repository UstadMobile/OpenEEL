package org.openeel.server.util.ext

import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadResult.Page.Companion.COUNT_UNDEFINED
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.header
import io.ktor.server.response.respond
import org.koin.core.scope.Scope
import org.koin.ktor.ext.getKoin
import org.openeel.datalayer.AuthenticatedUserPrincipalId
import org.openeel.lib.dataloadstate.DataLayerHeaders
import org.openeel.datalayer.respect.model.SchoolDirectoryEntry
import org.openeel.datalayer.school.domain.GetPermissionLastModifiedUseCase
import org.openeel.datalayer.shared.ModelWithTimes
import org.openeel.datalayer.shared.maxLastStoredOrNull
import org.openeel.lib.dataloadstate.ktorserver.validateIfNotModifiedSince
import org.openeel.lib.dataloadstate.throwable.ForbiddenException
import org.openeel.shared.domain.account.UserAccount
import org.openeel.shared.util.di.UserAccountScopeId
import org.openeel.shared.util.di.SchoolDirectoryEntryScopeId
import kotlin.time.Clock

/**
 * The virtual host being used. Used on the server to scope dependencies.
 */
val ApplicationCall.virtualHost: Url
    get() = request.virtualHost

/**
 * Respect schools are handled using virtual hosting e.g. by subdomains. - see
 * SchoolDirectoryEntryScopeId and AppKoinModule.
 */
fun ApplicationCall.getSchoolKoinScope(): Scope {
    return getKoin().getOrCreateScope<SchoolDirectoryEntry>(
        SchoolDirectoryEntryScopeId(
            request.virtualHost, null
        ).scopeId
    )
}

fun ApplicationCall.requireAccountScope(): Scope {
    val authPrincipalId: UserIdPrincipal = principal() ?:
        throw ForbiddenException("Not authenticated")

    val scopeId = UserAccountScopeId(
        request.virtualHost,
        AuthenticatedUserPrincipalId(authPrincipalId.name)
    ).scopeId

    return getKoin().getScopeOrNull(scopeId) ?: getKoin().createScope<UserAccount>(scopeId).also {
        it.linkTo(getSchoolKoinScope())
    }
}


suspend inline fun <reified T: Any> ApplicationCall.respondOffsetLimitPaging(
    params: PagingSource.LoadParams<Int>,
    pagingSource: PagingSource<Int, T>,
    getPermissionLastModifiedUseCase: GetPermissionLastModifiedUseCase? = null,
) {
    val consistentThrough = Clock.System.now()

    getPermissionLastModifiedUseCase?.also { getPermissionLastMod ->
        response.header(
            name = DataLayerHeaders.XPermissionsLastModified,
            value = getPermissionLastMod().toString(),
        )
    }

    val pagingLoadResult = pagingSource.load(params)

    when(pagingLoadResult) {
        is PagingSource.LoadResult.Page -> {
            val unwrappedList = pagingLoadResult.data
            val firstItem = unwrappedList.firstOrNull()
            val modelsWithTimes = if(firstItem is ModelWithTimes) {
                @Suppress("UNCHECKED_CAST")
                unwrappedList as List<ModelWithTimes>
            }else {
                null
            }

            response.header(
                name = DataLayerHeaders.XConsistentThrough,
                value = consistentThrough.toString()
            )

            if(pagingLoadResult.itemsBefore != COUNT_UNDEFINED &&
                pagingLoadResult.itemsAfter != COUNT_UNDEFINED) {
                val totalItems = pagingLoadResult.itemsBefore + pagingLoadResult.itemsAfter +
                        pagingLoadResult.data.size
                response.header(DataLayerHeaders.XTotalCount, totalItems)
            }

            //As per README - the last-mod for validation purposes is actually the time stored,
            // not the time originally modified (possibly on other device).
            val maxLastStored = modelsWithTimes?.maxLastStoredOrNull()
            maxLastStored?.also { response.lastModified(it) }

            if(maxLastStored != null &&
                request.validateIfNotModifiedSince(maxLastStored)
            ) {
                respond(HttpStatusCode.NotModified)
                return
            }

            respond(message = unwrappedList)
        }

        is PagingSource.LoadResult.Error -> {
            throw pagingLoadResult.throwable
        }

        is PagingSource.LoadResult.Invalid<*, *> -> {
            respond(HttpStatusCode.BadRequest)
        }
    }
}
