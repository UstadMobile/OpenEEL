package world.respect.server.util.ext

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
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.lib.dataloadstate.DataLayerHeaders
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.school.domain.GetPermissionLastModifiedUseCase
import world.respect.datalayer.shared.ModelWithTimes
import world.respect.datalayer.shared.maxLastStoredOrNull
import world.respect.lib.dataloadstate.ktorserver.validateIfNotModifiedSince
import world.respect.lib.dataloadstate.throwable.ForbiddenException
import world.respect.shared.domain.account.RespectAccount
import world.respect.shared.util.di.RespectAccountScopeId
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId
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

    val scopeId = RespectAccountScopeId(
        request.virtualHost,
        AuthenticatedUserPrincipalId(authPrincipalId.name)
    ).scopeId

    return getKoin().getScopeOrNull(scopeId) ?: getKoin().createScope<RespectAccount>(scopeId).also {
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
