package world.respect.shared.domain.account.passkey

import world.respect.datalayer.db.school.entities.PersonPasskeyEntity

interface GetActivePersonPasskeysUseCase {
    suspend fun getActivePeronPasskeys(personGuid: String): List<PersonPasskeyEntity>

}