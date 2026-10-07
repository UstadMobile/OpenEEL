package org.openeel.shared.domain.account.authwithpassword

import io.ktor.http.Url
import org.openeel.credentials.passkey.RespectCredential
import org.openeel.credentials.passkey.RespectPasskeyCredential
import org.openeel.credentials.passkey.RespectPasswordCredential
import org.openeel.credentials.passkey.RespectQRBadgeCredential
import org.openeel.datalayer.SchoolDirectoryDataSource
import org.openeel.datalayer.db.RespectSchoolDatabase
import org.openeel.datalayer.db.school.adapters.toEntity
import org.openeel.datalayer.db.school.adapters.toModel
import org.openeel.datalayer.db.school.adapters.toPersonEntities
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.libutil.ext.randomString
import org.openeel.libxxhash.XXStringHasher
import org.openeel.shared.domain.account.AuthResponse
import org.openeel.datalayer.school.model.AuthToken
import org.openeel.datalayer.school.model.DeviceInfo
import org.openeel.lib.dataloadstate.throwable.ForbiddenException
import org.openeel.lib.dataloadstate.throwable.withHttpStatus
import org.openeel.shared.domain.account.authenticatepassword.AuthenticatePasswordUseCase
import org.openeel.shared.domain.account.authenticatepassword.AuthenticateQrBadgeUseCase
import org.openeel.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithCredentialUseCase
import org.openeel.shared.domain.account.passkey.VerifySignInWithPasskeyUseCase
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
    private val schoolDirectoryDataSource: SchoolDirectoryDataSource,
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
                val rpId = this@GetTokenAndUserProfileWithCredentialDbImpl.schoolDirectoryDataSource.schoolDirectoryEntryDataSource
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