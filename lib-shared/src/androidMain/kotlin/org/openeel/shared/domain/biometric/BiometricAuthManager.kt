package world.respect.shared.domain.biometric

import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CompletableDeferred

class BiometricAuthManager(private val activity: AppCompatActivity) {

    fun canAuthenticate(): Boolean {
        val manager = BiometricManager.from(activity)
        val canAuthenticate = manager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        return canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS
    }

    suspend fun authenticate(promptData: BiometricAuthUseCase.BiometricPromptData): BiometricAuthUseCase.BiometricResult {
        val deferred = CompletableDeferred<BiometricAuthUseCase.BiometricResult>()
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                deferred.complete(BiometricAuthUseCase.BiometricResult.Success)
            }

            override fun onAuthenticationFailed() {
                deferred.complete(BiometricAuthUseCase.BiometricResult.Failure())
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                when (errorCode) {
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_CANCELED -> deferred.complete(BiometricAuthUseCase.BiometricResult.Canceled)

                    else -> deferred.complete(
                        BiometricAuthUseCase.BiometricResult.Error(
                            errorCode,
                            errString.toString()
                        )
                    )
                }
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)
        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(promptData.title)

        promptData.subtitle?.also { builder.setSubtitle(it) }
        promptData.description?.also { builder.setDescription(it) }

        if (promptData.useDeviceCredential) {
            builder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
        } else {
            builder.setNegativeButtonText(promptData.negativeButtonText)
        }

        prompt.authenticate(builder.build())
        return deferred.await()
    }
}
