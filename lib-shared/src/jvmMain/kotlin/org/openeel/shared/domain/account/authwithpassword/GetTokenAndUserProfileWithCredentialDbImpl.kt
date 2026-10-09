package world.respect.shared.domain.account.authwithpassword

import io.ktor.http.Url
import world.respect.credentials.passkey.RespectCredential
import world.respect.credentials.passkey.RespectPasskeyCredential
import world.respect.credentials.passkey.RespectPasswordCredential
import world.respect.credentials.passkey.RespectQRBadgeCredential
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toEntity
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.db.school.adapters.toPersonEntities
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.libutil.ext.randomString
import world.respect.libxxhash.XXStringHasher
import world.respect.shared.domain.account.AuthResponse
import world.respect.datalayer.school.model.AuthToken
import world.respect.datalayer.school.model.DeviceInfo
import world.respect.lib.dataloadstate.throwable.ForbiddenException
import world.respect.lib.dataloadstate.throwable.withHttpStatus
import world.respect.shared.domain.account.authenticatepassword.AuthenticatePasswordUseCase
import world.respect.shared.domain.account.authenticatepassword.AuthenticateQrBadgeUseCase
import world.respect.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithCredentialUseCase
import world.respect.shared.domain.account.passkey.VerifySignInWithPasskeyUseCase
import java.lang.IllegalStateException



/**
 * @property schoolDb Uses the database directly because the SchoolDataSource itself requires
 *           an authenticated user. If this use case required a SchoolDataSource for authentication,
 *           this would create a chicken/egg scenario.
 */
class GetTokenAndUserProfileWithCredentialDbImpl(
    private val schoolUrl: Url,
    private val schoolDb: RespectSchoolDatabase,
    private val xxHash: XXStringHasher,
    private val verifyPasskeyUseCase: VerifySignInWithPasskeyUseCase?,
    private val respectAppDataSource: RespectAppDataSource,
    private val authenticatePasswordUseCase: AuthenticatePasswordUseCase,
    private val authenticateQrBadgeUseCase: AuthenticateQrBadgeUseCase
): GetTokenAndUserProfileWithCredentialUseCase {

    override suspend fun invoke(
        credential: RespectCredential,
        deviceInfo: DeviceInfo?
    ): AuthResponse {

        val authenticatedPerson = when(credential) {
            is RespectPasswordCredential -> {
                authenticatePasswordUseCase(credential).authenticatedPerson
            }

            is RespectPasskeyCredential -> {
                val rpId = respectAppDataSource.schoolDirectoryEntryDataSource
                    .getSchoolDirectoryEntryByUrl(schoolUrl).dataOrNull()?.rpId
                    ?: throw IllegalStateException("School $schoolUrl has no rpId")
                        .withHttpStatus(400)

                val verifyPasskeyUseCaseVal = verifyPasskeyUseCase
                    ?: throw IllegalStateException("Verify passkey use case not provided")

                verifyPasskeyUseCaseVal(
                    credential.passkeyWebAuthNResponse,
                    rpId = rpId
                )
                val passkeyId = credential.passkeyWebAuthNResponse.id
                val personPasskey = schoolDb.getPersonPasskeyEntityDao().findPersonPasskeyFromClientDataJson(
                    passkeyId
                ) ?: throw IllegalArgumentException().withHttpStatus(400)

                schoolDb.getPersonEntityDao().findByGuidNum(
                    personPasskey.ppPersonUidNum
                )?.toPersonEntities()?.toModel() ?: throw ForbiddenException("Person not found")
            }

            is RespectQRBadgeCredential -> {
                authenticateQrBadgeUseCase(credential).authenticatedPerson
            }
        }

        val token = AuthToken(
            accessToken = randomString(32),
            timeCreated = System.currentTimeMillis(),
            ttl = TOKEN_DEFAULT_TTL,
        )

        val personGuidHash = xxHash.hash(authenticatedPerson.guid)
        schoolDb.getAuthTokenEntityDao().insert(
            token.toEntity(
                pGuid = authenticatedPerson.guid,
                pGuidHash = personGuidHash,
                deviceInfo = deviceInfo,
            )
        )

        return AuthResponse(
            token = token,
            person = authenticatedPerson,
        )
    }

    companion object {

        const val TOKEN_DEFAULT_TTL = (60 * 60 * 24 * 365)//one year

    }
}