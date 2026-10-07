package org.openeel.shared.domain.account.passkey

import org.openeel.datalayer.db.SchoolDatabase
import org.openeel.datalayer.db.school.entities.PersonPasskeyEntity
import org.openeel.libxxhash.XXStringHasher

class GetActivePersonPasskeysDbImpl(
    private val schoolDb: SchoolDatabase,
    private val xxStringHasher: XXStringHasher
) : GetActivePersonPasskeysUseCase {

    override suspend fun getActivePeronPasskeys(personGuid: String): List<PersonPasskeyEntity> {
        val personGuidHash = xxStringHasher.hash(personGuid)
        return schoolDb.getPersonPasskeyEntityDao().getAllActivePasskeysList(personGuidHash)
    }
}

