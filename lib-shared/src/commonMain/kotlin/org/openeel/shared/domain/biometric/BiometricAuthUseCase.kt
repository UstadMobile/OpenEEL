package world.respect.shared.domain.biometric

interface BiometricAuthUseCase {

    sealed class BiometricResult {
        object Success : BiometricResult()
        data class Failure(val reason: String? = null) : BiometricResult()
        object Canceled : BiometricResult()
        data class Error(val code: Int, val message: String) : BiometricResult()
    }
    data class BiometricPromptData(
        val title: String,
        val subtitle: String? = null,
        val description: String? = null,
        val negativeButtonText: String,
        val useDeviceCredential: Boolean = false
    )

    suspend operator fun invoke(request: BiometricPromptData): BiometricResult
}
