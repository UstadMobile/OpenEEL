package org.openeel.shared.domain.account.validatepassword

import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.password_must_be_at_least
import org.openeel.shared.util.exception.withUiText
import org.openeel.shared.util.ext.asUiText

class ValidatePasswordUseCase {

    operator fun invoke(password: String) {
        val passwordTrimmed = password.trim()

        if(passwordTrimmed.length < MIN_LENGTH) {
            throw IllegalArgumentException("Password too short").withUiText(
                Res.string.password_must_be_at_least.asUiText()
            )
        }
    }

    companion object {

        const val MIN_LENGTH = 6

    }
}