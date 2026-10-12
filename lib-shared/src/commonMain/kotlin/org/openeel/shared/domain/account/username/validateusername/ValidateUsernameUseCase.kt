package org.openeel.shared.domain.account.username.validateusername

import org.jetbrains.compose.resources.StringResource
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.invalid_username
import org.openeel.shared.generated.resources.username_starts_with_number
import org.openeel.shared.generated.resources.username_too_long
import org.openeel.shared.generated.resources.username_too_short

/**
 * Validates whether a username meets all required criteria:
 * - Must not be too short or too long
 * - Must not start with a number
 * - Must only contain valid characters (letters, numbers, dots, underscores)
 */
data class ValidationResult(val errorMessage: StringResource? = null) {
    companion object {
        val Valid = ValidationResult()
        val TooShort = ValidationResult(Res.string.username_too_short)
        val TooLong = ValidationResult(Res.string.username_too_long)
        val StartsWithNumber = ValidationResult(Res.string.username_starts_with_number)
        val InvalidCharacters = ValidationResult(Res.string.invalid_username)
    }
}

class ValidateUsernameUseCase {
    operator fun invoke(username: String): ValidationResult {
        return when {
            username.length < MIN_LENGTH -> ValidationResult.TooShort
            username.length > MAX_LENGTH -> ValidationResult.TooLong
            username.firstOrNull()?.isDigit() == true -> ValidationResult.StartsWithNumber
            !username.all { isValidUsernameChar(it) } -> ValidationResult.InvalidCharacters
            else -> ValidationResult.Valid
        }
    }

    companion object {
        private const val MIN_LENGTH = 3
        private const val MAX_LENGTH = 30
        val ALLOWED_SPECIAL = setOf('.', '_')

        fun isValidUsernameChar(character: Char) = when {
            character.isLetter() -> true
            character.isDigit() -> true
            character in ALLOWED_SPECIAL -> true
            else -> false
        }
    }
}