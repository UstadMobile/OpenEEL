package world.respect.shared.domain.xapi.xapinanohttpd

import io.ktor.http.Url
import io.ktor.util.decodeBase64String
import org.koin.core.component.KoinComponent
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.lib.xapi.XapiResourceProvider
import world.respect.lib.xapi.exceptions.XapiException
import world.respect.lib.xapi.resources.XapiResource
import world.respect.shared.util.di.RespectAccountScopeId
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId

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
        val schoolDb: RespectSchoolDatabase = getKoin().getScope(schoolScope.scopeId).get()

        val xapiSession = schoolDb.getXapiSessionEntityDao().findByUidAsync(uid = basicAuthUser.toLong())
            ?: throw XapiException(403, "Invalid session")

        if(xapiSession.xseAuth != basicAuthPass)
            throw XapiException(403, "Invalid session")

        val accountScope = RespectAccountScopeId(
            schoolUrl = endpoint,
            accountPrincipalId = AuthenticatedUserPrincipalId(xapiSession.xseAccountPersonUid),
        )

        val schoolDataSource: SchoolDataSource = getKoin().getScope(accountScope.scopeId).get()
        return schoolDataSource.xapiResource
    }

}