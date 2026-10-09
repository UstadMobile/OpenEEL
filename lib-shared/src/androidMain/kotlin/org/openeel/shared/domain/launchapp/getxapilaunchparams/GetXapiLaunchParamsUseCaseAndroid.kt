package world.respect.shared.domain.launchapp.getxapilaunchparams

import io.ktor.http.Url
import io.ktor.util.encodeBase64
import kotlinx.coroutines.flow.first
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.xapi.adapters.identifierHash
import world.respect.datalayer.db.school.xapi.entities.XapiSessionEntity
import world.respect.lib.xapi.nanohttpd.XapiNanoHttpdApp
import world.respect.libutil.ext.appendAssignmentXapiSegment
import world.respect.libutil.ext.randomString
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.xapi.getxapilaunchurl.GetXapiLaunchUrlUseCase

class GetXapiLaunchParamsUseCaseAndroid(
    private val nanoHttpdApp: XapiNanoHttpdApp,
    private val schoolUrl: Url,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
    private val accountManager: RespectAccountManager,
    private val uidNumberMapper: UidNumberMapper,
    private val schoolDb: RespectSchoolDatabase,
) : GetXapiLaunchParamsUseCase {

    override suspend fun invoke(
        activityId: String,
        assignmentActivityId: String?,
        type: GetXapiLaunchUrlUseCase.LaunchType,
    ): XapiLaunchParams {
        val activeSession = accountManager.selectedAccountAndPersonFlow.first()
            ?: throw IllegalStateException("Cannot launch when there is no active person")

        val xapiSessionEntity = XapiSessionEntity(
            xseActorUid = activeSession.xapiAgent.identifierHash(uidNumberMapper),
            xseAccountPersonUid = authenticatedUser.guid,
            xseStartTime = System.currentTimeMillis(),
            xseAuth = randomString(10),
        )
        val xseUid = schoolDb.getXapiSessionEntityDao().insertAsync(xapiSessionEntity)
        val basicAuth = "${xseUid}:${xapiSessionEntity.xseAuth}".encodeBase64()

        val baseEndpoint = if(type == GetXapiLaunchUrlUseCase.LaunchType.WEBVIEW) {
            nanoHttpdApp.localUrlForEndpoint(schoolUrl)
        }else {
            schoolUrl
        }

        return XapiLaunchParams(
            activityId = activityId,
            endpoint =  baseEndpoint.let {
                if (assignmentActivityId != null) {
                    it.appendAssignmentXapiSegment(assignmentActivityId)
                } else {
                    it
                }
            },
            actor = activeSession.xapiAgent,
            auth = "Basic $basicAuth",
            registration = null,
        )
    }
}