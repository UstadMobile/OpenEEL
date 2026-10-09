package world.respect.shared.domain.account.passkey

import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.entities.PersonPasskeyEntity
import world.respect.libxxhash.XXStringHasher

class GetActivePersonPasskeysDbImpl(
    private val schoolDb: RespectSchoolDatabase,
    private val xxStringHasher: XXStringHasher
) : GetActivePersonPasskeysUseCase {

    override suspend fun getActivePeronPasskeys(personGuid: String): List<PersonPasskeyEntity> {
        val personGuidHash = xxStringHasher.hash(personGuid)
        return schoolDb.getPersonPasskeyEntityDao().getAllActivePasskeysList(personGuidHash)
    }
}

