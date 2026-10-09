package world.respect.shared.domain.biometric

import kotlinx.coroutines.CompletableDeferred

data class BiometricAuthJob(
    val request: BiometricAuthUseCase.BiometricPromptData,
    val response: CompletableDeferred<BiometricAuthUseCase.BiometricResult> = CompletableDeferred()
)
