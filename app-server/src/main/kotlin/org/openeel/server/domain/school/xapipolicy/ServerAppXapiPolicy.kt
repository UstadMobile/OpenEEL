package org.openeel.server.domain.school.xapipolicy

import io.ktor.http.HttpStatusCode
import org.openeel.datalayer.AuthenticatedUserPrincipalId
import org.openeel.datalayer.UidNumberMapper
import org.openeel.datalayer.db.SchoolDatabase
import org.openeel.datalayer.school.model.PersonRoleEnum
import org.openeel.lib.xapi.OpenEelXapiConstants
import org.openeel.lib.xapi.auth.requireNotNullOrThrowXapiException
import org.openeel.lib.xapi.exceptions.XapiException
import org.openeel.lib.xapi.model.XapiDocument
import org.openeel.lib.xapi.model.XapiStatement
import org.openeel.lib.xapi.permissionmanager.policy.XapiActivityProfilePolicy
import org.openeel.lib.xapi.permissionmanager.policy.XapiAgentProfileResourcePolicy
import org.openeel.lib.xapi.permissionmanager.policy.XapiDocumentResourcePolicy
import org.openeel.lib.xapi.permissionmanager.policy.XapiPolicy
import org.openeel.lib.xapi.permissionmanager.policy.XapiStateResourcePolicy
import org.openeel.lib.xapi.permissionmanager.policy.XapiStatementsResourcePolicy
import org.openeel.lib.xapi.resources.ISingleDocumentParams
import org.openeel.lib.xapi.resources.XapiActivityProfileResource
import org.openeel.lib.xapi.resources.XapiStatementsResource

class ServerAppXapiPolicy(
    private val authenticatedUser: AuthenticatedUserPrincipalId?,
    private val uidNumberMapper: UidNumberMapper,
    private val schoolDb: SchoolDatabase,
): XapiPolicy {

    private suspend fun isAuthenticatedUserAdminOrTeacher() : Boolean{
        val authenticatedUid = authenticatedUser?.guid ?: return false

        return schoolDb.getPersonRoleEntityDao().personHasRole(
            personUidHash = uidNumberMapper(authenticatedUid),
            roles = listOf(
                PersonRoleEnum.TEACHER.flag,
                PersonRoleEnum.SITE_ADMINISTRATOR.flag,
                PersonRoleEnum.SYSTEM_ADMINISTRATOR.flag
            )
        )
    }

    override val statements: XapiStatementsResourcePolicy = object : XapiStatementsResourcePolicy {
        override suspend fun transformReadParams(params: XapiStatementsResource.GetStatementParams): XapiStatementsResource.GetStatementParams {
            return params
        }

        override suspend fun transformWrite(statement: XapiStatement): XapiStatement {
            return statement
        }
    }

    class PassThroughXapiDocumentPolicy<MultiDocParams: Any, SingleDocParams: ISingleDocumentParams>:
            XapiDocumentResourcePolicy<MultiDocParams, SingleDocParams> {

        override suspend fun transformReadSingleDocParams(params: SingleDocParams): SingleDocParams {
            return params
        }

        override suspend fun transformReadMultiDocParams(params: MultiDocParams): MultiDocParams {
            return params
        }

        override suspend fun transformPostDoc(
            document: XapiDocument,
            params: SingleDocParams
        ): XapiDocumentResourcePolicy.TransformedWrite<SingleDocParams> {
            return XapiDocumentResourcePolicy.TransformedWrite(document, params)
        }

        override suspend fun transformPutDoc(
            document: XapiDocument,
            params: SingleDocParams
        ): XapiDocumentResourcePolicy.TransformedWrite<SingleDocParams> {
            return XapiDocumentResourcePolicy.TransformedWrite(document, params)
        }

        override suspend fun transformDeleteDoc(params: SingleDocParams): SingleDocParams {
            return params
        }
    }

    override val activityProfile: XapiActivityProfilePolicy = object : XapiActivityProfilePolicy {

        override suspend fun transformReadSingleDocParams(
            params: XapiActivityProfileResource.SingleDocumentParams
        ): XapiActivityProfileResource.SingleDocumentParams {
            if(params.profileId == OpenEelXapiConstants.ACTIVITY_PROFILEID_OPDS_COLLECTION) {
                return params
            }

            requireNotNullOrThrowXapiException(authenticatedUser)

            return params
        }

        override suspend fun transformReadMultiDocParams(params: XapiActivityProfileResource.MultiDocParams): XapiActivityProfileResource.MultiDocParams {
            requireNotNullOrThrowXapiException(authenticatedUser)
            return params
        }

        override suspend fun transformPostDoc(
            document: XapiDocument,
            params: XapiActivityProfileResource.SingleDocumentParams
        ): XapiDocumentResourcePolicy.TransformedWrite<XapiActivityProfileResource.SingleDocumentParams> {
            if(!isAuthenticatedUserAdminOrTeacher())
                throw XapiException(HttpStatusCode.Unauthorized.value, "Unauthorized")

            return XapiDocumentResourcePolicy.TransformedWrite(document, params)
        }

        override suspend fun transformPutDoc(
            document: XapiDocument,
            params: XapiActivityProfileResource.SingleDocumentParams
        ): XapiDocumentResourcePolicy.TransformedWrite<XapiActivityProfileResource.SingleDocumentParams> {
            if(!isAuthenticatedUserAdminOrTeacher())
                throw XapiException(HttpStatusCode.Unauthorized.value, "Unauthorized")

            return XapiDocumentResourcePolicy.TransformedWrite(document, params)
        }

        override suspend fun transformDeleteDoc(params: XapiActivityProfileResource.SingleDocumentParams): XapiActivityProfileResource.SingleDocumentParams {
            if(!isAuthenticatedUserAdminOrTeacher())
                throw XapiException(HttpStatusCode.Unauthorized.value, "Unauthorized")

            return params
        }
    }

    override val state: XapiStateResourcePolicy by lazy {
        PassThroughXapiDocumentPolicy()
    }

    override val agentProfile: XapiAgentProfileResourcePolicy by lazy {
        PassThroughXapiDocumentPolicy()
    }
}