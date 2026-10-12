package org.openeel.shared.domain.account.validateauth

import org.openeel.datalayer.db.SchoolDatabase
import org.openeel.datalayer.AuthenticatedUserPrincipalId
import org.openeel.libutil.util.time.systemTimeInMillis

class ValidateAuthorizationUseCaseDbImpl(
    private val schoolDb: SchoolDatabase,
): ValidateAuthorizationUseCase {

    override suspend fun invoke(
        credential: ValidateAuthorizationUseCase.AuthorizationCredential
    ): AuthenticatedUserPrincipalId? {
        when(credential) {
            is ValidateAuthorizationUseCase.BearerTokenCredential -> {
                val dbToken = schoolDb.getAuthTokenEntityDao().findByToken(
                    credential.token, systemTimeInMillis(),
                ) ?: return null

                return AuthenticatedUserPrincipalId(dbToken.atPGuid)
            }

            else -> {
                throw IllegalArgumentException()
            }
        }
    }
}