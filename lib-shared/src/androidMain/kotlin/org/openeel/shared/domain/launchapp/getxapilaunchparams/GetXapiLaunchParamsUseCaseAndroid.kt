package org.openeel.shared.domain.launchapp.getxapilaunchparams

import io.ktor.http.Url
import io.ktor.util.encodeBase64
import kotlinx.coroutines.flow.first
import org.openeel.datalayer.AuthenticatedUserPrincipalId
import org.openeel.datalayer.UidNumberMapper
import org.openeel.datalayer.db.RespectSchoolDatabase
import org.openeel.datalayer.db.school.xapi.adapters.identifierHash
import org.openeel.datalayer.db.school.xapi.entities.XapiSessionEntity
import org.openeel.lib.xapi.nanohttpd.XapiNanoHttpdApp
import org.openeel.libutil.ext.appendAssignmentXapiSegment
import org.openeel.libutil.ext.randomString
import org.openeel.shared.domain.account.RespectAccountManager
import org.openeel.shared.domain.xapi.getxapilaunchurl.GetXapiLaunchUrlUseCase

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