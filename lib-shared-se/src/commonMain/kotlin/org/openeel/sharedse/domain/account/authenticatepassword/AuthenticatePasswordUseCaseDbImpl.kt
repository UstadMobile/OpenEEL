package world.respect.sharedse.domain.account.authenticatepassword

import io.github.aakira.napier.Napier
import world.respect.credentials.passkey.RespectPasswordCredential
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.shared.domain.account.authenticatepassword.AuthenticatePasswordUseCase
import world.respect.shared.domain.account.setpassword.EncryptPersonPasswordUseCase
import io.ktor.util.decodeBase64Bytes
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.db.school.adapters.toPersonEntities
import world.respect.lib.dataloadstate.throwable.ForbiddenException
import world.respect.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithCredentialUseCase.Companion.LOGTAG_AUTH

class AuthenticatePasswordUseCaseDbImpl(
    private val schoolDb: RespectSchoolDatabase,
    private val encryptPersonPasswordUseCase: EncryptPersonPasswordUseCase,
    private val uidNumberMapper: UidNumberMapper,
) : AuthenticatePasswordUseCase {

    override suspend fun invoke(
        credential: RespectPasswordCredential
    ) : AuthenticatePasswordUseCase.Response {
        val personEntity = schoolDb.getPersonEntityDao().findByUsername(credential.username)
            ?: throw ForbiddenException("Invalid username/password").also {
                Napier.d(tag = LOGTAG_AUTH) { "AuthenticatePasswordUseCaseDbImpl: user ${credential.username} not found" }
            }

        val uidNum = uidNumberMapper(personEntity.person.pGuid)
        val expectedPassword = schoolDb.getPersonPasswordEntityDao().findByUid(
            uidNum = uidNum
        ) ?: throw ForbiddenException("Invalid username/password").also {
            Napier.d(tag = LOGTAG_AUTH) { "AuthenticatePasswordUseCaseDbImpl: password not found for ${credential.username} uid=$uidNum" }
        }

        val credentialEncrypted = encryptPersonPasswordUseCase(
            EncryptPersonPasswordUseCase.Request(
                personGuid = personEntity.person.pGuid,
                password = credential.password,
                salt = expectedPassword.authSalt,
            )
        )

        if (
            !credentialEncrypted.authEncoded.decodeBase64Bytes().contentEquals(
                expectedPassword.authEncoded.decodeBase64Bytes()
            )
        ) {
            throw ForbiddenException("Invalid username/password").also {
                Napier.d(tag = LOGTAG_AUTH) {
                    "AuthenticatePasswordUseCaseDbImpl: password for ${credential.username} does not match"
                }
            }
        }

        return AuthenticatePasswordUseCase.Response(
            authenticatedPerson = personEntity.toPersonEntities().toModel()
        ).also {
            Napier.d(tag = LOGTAG_AUTH) { "AuthenticatePasswordUseCaseDbImpl: Authenticate ${credential.username} with password successful" }
        }
    }
}