package world.respect.datalayer.db.school.xapi


import io.ktor.http.Url
import kotlinx.serialization.json.Json
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.GetAuthenticatedPersonUseCase
import world.respect.lib.xapi.auth.GetAuthenticatedXapiAgentsUseCase
import world.respect.lib.xapi.resources.local.XapiActivitiesResourceLocal
import world.respect.lib.xapi.resources.local.XapiActivityProfileResourceLocal
import world.respect.lib.xapi.resources.local.XapiAgentProfileResourceLocal
import world.respect.lib.xapi.resources.local.XapiAgentsResourceLocal
import world.respect.lib.xapi.resources.local.XapiResourceLocal
import world.respect.lib.xapi.resources.local.XapiStateResourceLocal
import world.respect.lib.xapi.resources.local.XapiStatementsResourceLocal

class XapiResourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
    private val json: Json,
    private val schoolUrl: Url,
    private val getAuthenticatedXapiAgentsUseCase: GetAuthenticatedXapiAgentsUseCase,
): XapiResourceLocal {

    private val getAuthenticatedPersonUseCase by lazy {
        GetAuthenticatedPersonUseCase(
            authenticatedUser, schoolDb, uidNumberMapper
        )
    }

    override val statements: XapiStatementsResourceLocal by lazy {
        XapiStatementsResourceDb(
            schoolDb = schoolDb,
            authenticatedUser = authenticatedUser,
            getAuthenticatedPersonUseCase = getAuthenticatedPersonUseCase,
            schoolUrl = schoolUrl,
            uidNumberMapper = uidNumberMapper,
            xapiActivitiesResourceLocal = activities,
            xapiAgentsResourceLocal = agents,
            json = json,
        )
    }

    override val agents: XapiAgentsResourceLocal by lazy {
        XapiAgentsResourceDb(
            schoolDb = schoolDb,
            authenticatedUser = authenticatedUser,
            uidNumberMapper = uidNumberMapper,
        )
    }

    override val activities: XapiActivitiesResourceLocal by lazy{
        XapiActivitiesResourceDb(
            schoolDb = schoolDb,
            authenticatedUser = authenticatedUser,
            uidNumberMapper = uidNumberMapper,
            json = json,
        )
    }

    override val activityProfile: XapiActivityProfileResourceLocal by lazy {
        XapiActivityProfileResourceDb(
            schoolDb = schoolDb,
            json = json,
        )
    }

    override val agentProfile: XapiAgentProfileResourceLocal by lazy {
        XapiAgentProfileResourceDb(
            schoolDb = schoolDb,
            json = json,
            getAuthenticatedXapiAgentsUseCase = getAuthenticatedXapiAgentsUseCase,
        )
    }

    override val state: XapiStateResourceLocal by lazy {
        XapiStateResourceDb(
            schoolDb = schoolDb,
            json = json,
            getAuthenticatedXapiAgentsUseCase = getAuthenticatedXapiAgentsUseCase,
        )
    }

    override fun close() {
        schoolDb.close()
    }
}