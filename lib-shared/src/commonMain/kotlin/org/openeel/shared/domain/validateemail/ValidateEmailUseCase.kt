package world.respect.shared.domain.validateemail

class ValidateEmailUseCase() {

    operator fun invoke(email: String): Boolean {
        val trimmed = email.trim()
        val atPos = trimmed.indexOf('@')

        // Must have '@'
        if (atPos == -1) return false

        // Must have '.' after '@'
        if (trimmed.indexOf('.', atPos) == -1) return false

        // Must not contain spaces, [ ], or '\'
        if (trimmed.any { it.isWhitespace() || it == '[' || it == ']' || it == '\\' }) return false

        return true
    }

}