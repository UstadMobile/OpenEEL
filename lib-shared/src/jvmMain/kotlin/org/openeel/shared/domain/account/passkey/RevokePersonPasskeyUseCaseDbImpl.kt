package org.openeel.shared.domain.account.passkey

import org.openeel.datalayer.db.RespectSchoolDatabase
import org.openeel.libxxhash.XXStringHasher

class RevokePersonPasskeyUseCaseDbImpl(
    private val schoolDb: RespectSchoolDatabase,
    private val xxStringHasher: XXStringHasher
) : RevokePasskeyUseCase {

    override suspend fun invoke(personGuid: String){
        val personGuidHash = xxStringHasher.hash(personGuid)
        //return schoolDb.getPersonPasskeyEntityDao().revokePersonPasskey(personGuidHash)
    }
}

