package world.respect.shared.domain.account.passkey

import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.libxxhash.XXStringHasher

class RevokePersonPasskeyUseCaseDbImpl(
    private val schoolDb: RespectSchoolDatabase,
    private val xxStringHasher: XXStringHasher
) : RevokePasskeyUseCase {

    override suspend fun invoke(personGuid: String){
        val personGuidHash = xxStringHasher.hash(personGuid)
        //return schoolDb.getPersonPasskeyEntityDao().revokePersonPasskey(personGuidHash)
    }
}

