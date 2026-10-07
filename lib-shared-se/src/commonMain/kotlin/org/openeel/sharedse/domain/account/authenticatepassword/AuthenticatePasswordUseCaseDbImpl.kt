package org.openeel.sharedse.domain.account.authenticatepassword

import io.github.aakira.napier.Napier
import org.openeel.credentials.passkey.OpenEelPasswordCredential
import org.openeel.datalayer.UidNumberMapper
import org.openeel.datalayer.db.SchoolDatabase
import org.openeel.shared.domain.account.authenticatepassword.AuthenticatePasswordUseCase
import org.openeel.shared.domain.account.setpassword.EncryptPersonPasswordUseCase
import io.ktor.util.decodeBase64Bytes
import org.openeel.datalayer.db.school.adapters.toModel
import org.openeel.datalayer.db.school.adapters.toPersonEntities
import org.openeel.lib.dataloadstate.throwable.ForbiddenException
import org.openeel.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithCredentialUseCase.Companion.LOGTAG_AUTH

class AuthenticatePasswordUseCaseDbImpl(
    private val schoolDb: SchoolDatabase,
    private val encryptPersonPasswordUseCase: EncryptPersonPasswordUseCase,
    private val uidNumberMapper: UidNumberMapper,
) : AuthenticatePasswordUseCase {

    override suspend fun invoke(
        credential: OpenEelPasswordCredential
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