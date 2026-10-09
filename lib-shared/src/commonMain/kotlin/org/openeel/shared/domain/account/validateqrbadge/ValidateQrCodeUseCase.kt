package world.respect.shared.domain.account.validateqrbadge

import io.ktor.http.Url
import org.jetbrains.compose.resources.StringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.*

data class QrValidationResult(
    val isValid: Boolean = true,
    val errorMessage: StringResource? = null
) {
    companion object {
        val Valid = QrValidationResult(isValid = true)
        val InvalidUrl = QrValidationResult(
            isValid = false,
            errorMessage = Res.string.qr_code_invalid_format
        )
    }
}

class ValidateQrCodeUseCase(
    private val schoolUrl: Url?
) {

    operator fun invoke(
        qrCodeUrl: String,
        personGuid: String? = null,
    ): QrValidationResult {
        // 1. Validate URL format
        if (!isValidUrlFormat(qrCodeUrl)) return QrValidationResult.InvalidUrl

        // 2. Validate starts with school URL
        if (!isValidSchoolUrl(qrCodeUrl)) return QrValidationResult.InvalidUrl

        // 3. Validate contains /personqrbadge section
        if (!hasValidBadgeSection(qrCodeUrl)) return QrValidationResult.InvalidUrl

        return QrValidationResult.Valid
    }

    private fun isValidUrlFormat(qrCodeUrl: String): Boolean {
        return try {
            Url(qrCodeUrl)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun isValidSchoolUrl(qrCodeUrl: String): Boolean {
        return if (schoolUrl != null) {
            qrCodeUrl.startsWith(schoolUrl.toString())
        } else {
            true
        }
    }

    private fun hasValidBadgeSection(qrCodeUrl: String): Boolean {
        return qrCodeUrl.contains("/personqrbadge", ignoreCase = true)
    }
}