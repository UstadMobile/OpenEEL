package world.respect.server.account.invite.username.checkusernameunique

import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.shared.domain.account.username.checkusernameunique.CheckUsernameUniqueUseCase

class CheckUsernameUniqueUseCaseServer(
    private val schoolDb:  RespectSchoolDatabase
) : CheckUsernameUniqueUseCase {

    override suspend fun invoke(username: String): Boolean {
        return !schoolDb.getPersonEntityDao().getUsernameAlreadyExists(
            username = username
        )
    }
}