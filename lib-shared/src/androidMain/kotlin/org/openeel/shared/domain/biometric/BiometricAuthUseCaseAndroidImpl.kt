package world.respect.shared.domain.biometric

import kotlinx.coroutines.channels.Channel

class BiometricAuthUseCaseAndroidImpl : BiometricAuthUseCase {

    val requestChannel = Channel<BiometricAuthJob>(Channel.UNLIMITED)

    override suspend fun invoke(
        request: BiometricAuthUseCase.BiometricPromptData
    ): BiometricAuthUseCase.BiometricResult {
        val job = BiometricAuthJob(request)
        requestChannel.send(job)
        return job.response.await()
    }
}
