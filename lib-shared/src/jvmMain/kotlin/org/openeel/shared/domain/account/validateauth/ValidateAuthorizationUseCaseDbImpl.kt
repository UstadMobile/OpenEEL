package world.respect.shared.domain.account.validateauth

import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.libutil.util.time.systemTimeInMillis

class ValidateAuthorizationUseCaseDbImpl(
    private val schoolDb: RespectSchoolDatabase,
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