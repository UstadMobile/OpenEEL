package org.openeel.shared.domain.account.passkey

import org.openeel.datalayer.db.school.entities.PersonPasskeyEntity

interface GetActivePersonPasskeysUseCase {
    suspend fun getActivePeronPasskeys(personGuid: String): List<PersonPasskeyEntity>

}