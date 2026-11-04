package org.openeel.shared.domain.xapi.xapinanohttpd

import io.ktor.http.Url
import io.ktor.util.decodeBase64String
import org.koin.core.component.KoinComponent
import org.openeel.datalayer.AuthenticatedUserPrincipalId
import org.openeel.datalayer.SchoolDataSource
import org.openeel.datalayer.db.SchoolDatabase
import org.openeel.lib.xapi.XapiResourceProvider
import org.openeel.lib.xapi.exceptions.XapiException
import org.openeel.lib.xapi.resources.XapiResource
import org.openeel.shared.util.di.UserAccountScopeId
import org.openeel.shared.util.di.SchoolDirectoryEntryScopeId

class XapiResourceProviderAndroid: XapiResourceProvider, KoinComponent {

    override suspend fun provideXapiResource(
        endpoint: Url,
        authentication: String?,
    ): XapiResource {
        if(authentication == null)
            throw XapiException(403, "No authorization header provided")

        val (basicAuthUser, basicAuthPass) = authentication.substringAfter("Basic")
            .trim()
            .decodeBase64String()
            .split(":", limit = 2).let {
                Pair(it.first(), it.last())
            }

        val schoolScope = SchoolDirectoryEntryScopeId(schoolUrl = endpoint , accountPrincipalId = null)
        val schoolDb: SchoolDatabase = getKoin().getScope(schoolScope.scopeId).get()

        val xapiSession = schoolDb.getXapiSessionEntityDao().findByUidAsync(uid = basicAuthUser.toLong())
            ?: throw XapiException(403, "Invalid session")

        if(xapiSession.xseAuth != basicAuthPass)
            throw XapiException(403, "Invalid session")

        val accountScope = UserAccountScopeId(
            schoolUrl = endpoint,
            accountPrincipalId = AuthenticatedUserPrincipalId(xapiSession.xseAccountPersonUid),
        )

        val schoolDataSource: SchoolDataSource = getKoin().getScope(accountScope.scopeId).get()
        return schoolDataSource.xapiResource
    }

}