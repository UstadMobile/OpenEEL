package org.openeel.server.account.invite.username.checkusernameunique

import org.openeel.datalayer.db.SchoolDatabase
import org.openeel.shared.domain.account.username.checkusernameunique.CheckUsernameUniqueUseCase

class CheckUsernameUniqueUseCaseServer(
    private val schoolDb:  SchoolDatabase
) : CheckUsernameUniqueUseCase {

    override suspend fun invoke(username: String): Boolean {
        return !schoolDb.getPersonEntityDao().getUsernameAlreadyExists(
            username = username
        )
    }
}