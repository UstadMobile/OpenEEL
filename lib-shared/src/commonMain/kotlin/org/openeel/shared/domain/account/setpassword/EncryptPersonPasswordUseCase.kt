package world.respect.shared.domain.account.setpassword

import world.respect.datalayer.school.model.PersonPassword
import world.respect.libutil.ext.randomString

interface EncryptPersonPasswordUseCase {

    data class Request(
        val personGuid: String,
        val password: String,
        val salt: String = randomString(DEFAULT_SALT_LEN),
    )

    operator fun invoke(
        request: Request,
    ): PersonPassword

    companion object {

        const val DEFAULT_SALT_LEN = 16

    }
}