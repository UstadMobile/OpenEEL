package world.respect.shared.domain.biometric

import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class BiometricAuthProcessor(
    activity: AppCompatActivity,
    private val jobChannel: Channel<BiometricAuthJob>,
    private val processOnScope: CoroutineScope,
) {
    val biometricManager = BiometricAuthManager(activity)

    suspend fun receiveJobs() {
        for (job in jobChannel) {
            processOnScope.launch {
                try {
                    if (!biometricManager.canAuthenticate()) {
                        job.response.complete(
                            BiometricAuthUseCase.BiometricResult.Error(0, "not available")
                        )
                        return@launch
                    }

                    val result = biometricManager.authenticate(job.request)
                    job.response.complete(result)

                } catch (t: Throwable) {
                    job.response.complete(
                        BiometricAuthUseCase.BiometricResult.Error(
                            0,
                            t.message ?: ""
                        )
                    )
                }
            }
        }
    }
}

