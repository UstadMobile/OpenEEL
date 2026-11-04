package org.openeel.server.account.invite.username.checkusernameunique

import org.openeel.datalayer.db.RespectSchoolDatabase
import org.openeel.shared.domain.account.username.checkusernameunique.CheckUsernameUniqueUseCase

class CheckUsernameUniqueUseCaseServer(
    private val schoolDb:  RespectSchoolDatabase
) : CheckUsernameUniqueUseCase {

    override suspend fun invoke(username: String): Boolean {
        return !schoolDb.getPersonEntityDao().getUsernameAlreadyExists(
            username = username
        )
    }
}