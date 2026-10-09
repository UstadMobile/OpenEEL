package org.openeel.shared.domain.account.setpassword

import org.openeel.datalayer.school.model.PersonPassword
import org.openeel.libutil.ext.randomString

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