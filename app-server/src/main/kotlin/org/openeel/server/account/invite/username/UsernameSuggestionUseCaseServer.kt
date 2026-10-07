package org.openeel.server.account.invite.username

import org.koin.core.component.KoinComponent
import org.openeel.datalayer.db.SchoolDatabase
import org.openeel.lib.dataloadstate.throwable.withHttpStatus
import org.openeel.shared.domain.account.username.UsernameSuggestionUseCase
import org.openeel.shared.domain.account.username.filterusername.FilterUsernameUseCase

class UsernameSuggestionUseCaseServer(
    private val filterUsernameUseCase: FilterUsernameUseCase,
    private val schoolDb: SchoolDatabase,
): UsernameSuggestionUseCase, KoinComponent  {

    companion object {
        private const val MAX_ATTEMPTS = 1000
    }

    override suspend operator fun invoke(name: String): String {
        val baseUsername = filterUsernameUseCase(name, "")
        var suggestedUsername: String
        val personDb = schoolDb.getPersonEntityDao()
        for (index in 0 until MAX_ATTEMPTS) {
            suggestedUsername = if (index == 0) baseUsername else "$baseUsername$index"
            if (personDb.findByUsername(suggestedUsername) == null) {
                return suggestedUsername
            }
        }

        throw IllegalArgumentException("Unable to generate a unique username")
            .withHttpStatus(404)
    }
}
