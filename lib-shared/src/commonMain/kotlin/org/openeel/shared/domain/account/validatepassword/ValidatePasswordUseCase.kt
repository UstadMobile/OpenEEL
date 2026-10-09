package world.respect.shared.domain.account.validatepassword

import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.password_must_be_at_least
import world.respect.shared.util.exception.withUiText
import world.respect.shared.util.ext.asUiText

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