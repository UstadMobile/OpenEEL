package org.openeel.server.account.openid

import io.ktor.server.auth.oidc.OidcToken
import io.ktor.utils.io.ExperimentalKtorApi
import org.openeel.app.userdirectory.model.PersonRole
import org.openeel.app.userdirectory.model.PersonRoleEnum
import org.openeel.datalayer.AuthenticatedUserPrincipalId
import org.openeel.datalayer.SchoolDataSourceLocal
import org.openeel.datalayer.UidNumberMapper
import org.openeel.datalayer.db.RespectSchoolDatabase
import org.openeel.datalayer.db.school.adapters.toEntity
import org.openeel.datalayer.school.model.AuthToken
import org.openeel.datalayer.school.model.Person
import org.openeel.datalayer.school.model.PersonGenderEnum
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.dataloadstate.throwable.withHttpStatus
import org.openeel.libutil.ext.randomString
import org.openeel.server.ServerAccountScopeManager
import org.openeel.shared.domain.account.AuthResponse
import org.openeel.shared.domain.account.authwithpassword.GetTokenAndUserProfileWithCredentialDbImpl

/**
 * Creates a Respect account session from an access token validated by server
 * Ktor validated access token prncipal https://ktor.io/docs/server-oidc-resource-server.html
 */
@OptIn(ExperimentalKtorApi::class)
class GetOpenIdLoginResponseUseCaseServer(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    private val serverAccountScopeManager: ServerAccountScopeManager,
) {

    suspend operator fun invoke(accessToken: OidcToken.Access): AuthResponse {
        val claims = accessToken.claims
        // issuer Identifier is a case-sensitive URL using the https scheme that
        // contains scheme, host.
        //subject is Locally unique and never reassigned identifier within the Issuer
        // for the End-User, which is intended to be consumed by the Client.
        // https://openid.net/specs/openid-connect-core-1_0-errata2.html#ClaimStability
        claims.issuer ?: throw IllegalArgumentException("OpenID issuer is missing")
            .withHttpStatus(401)
        val subject = claims.subject ?: throw IllegalArgumentException("OpenID subject is missing")
            .withHttpStatus(401)
        val personGuid = subject
        val accountScope = serverAccountScopeManager.getOrCreateAccountScope(
            AuthenticatedUserPrincipalId(personGuid)
        )
        val personDataSource = accountScope.get<SchoolDataSourceLocal>().personDataSource
        val existingPerson = personDataSource.findByGuid(
            loadParams = DataLoadParams(),
            guid = personGuid,
        ).dataOrNull()

        val person = existingPerson ?: run {
            val roleClaim = claims.claimString("respect_role")
            val role = roleClaim?.let { value ->
                PersonRoleEnum.entries.firstOrNull { it.value == value }
                    ?: throw IllegalArgumentException(value).withHttpStatus(403)
            } ?: PersonRoleEnum.STUDENT
            val genderClaim = claims.claimString("gender")

            Person(
                guid = personGuid,
                username = accessToken.userInfo?.preferredUsername
                    ?: claims.claimString("preferred_username"),
                givenName = (accessToken.userInfo?.givenName
                    ?: claims.claimString("given_name")).requireClaim("given_name"),
                familyName = (accessToken.userInfo?.familyName
                    ?: claims.claimString("family_name")).requireClaim("family_name"),
                email = accessToken.userInfo?.email ?: claims.claimString("email"),
                gender = PersonGenderEnum.entries.firstOrNull {
                    it.value.equals(genderClaim, ignoreCase = true)
                } ?: PersonGenderEnum.UNSPECIFIED,
                roles = listOf(PersonRole(isPrimaryRole = true, roleEnum = role)),
            )
        }

        if (existingPerson == null) {
            personDataSource.updateLocal(listOf(person))
        }

        val token = AuthToken(
            accessToken = randomString(32),
            timeCreated = System.currentTimeMillis(),
            ttl = GetTokenAndUserProfileWithCredentialDbImpl.TOKEN_DEFAULT_TTL,
        )
        schoolDb.getAuthTokenEntityDao().insert(
            token.toEntity(
                pGuid = person.guid,
                pGuidHash = uidNumberMapper(person.guid),
            )
        )

        return AuthResponse(
            token = token,
            person = person,
        )
    }

    private fun String?.requireClaim(claimName: String): String =
        this?.takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("$claimName' is required")
                .withHttpStatus(400)
}
