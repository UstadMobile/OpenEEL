package world.respect.credentials.password

import android.util.Log
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.CreateCredentialNoCreateOptionException
import world.respect.credentials.passkey.password.SavePasswordUseCase
import world.respect.shared.domain.activitycontextjobprocessor.EnqueueActivityContextJobUseCase

class SavePasswordUseCaseAndroidImpl(
    private val enqueueActivityContextJobUseCase: EnqueueActivityContextJobUseCase,
) : SavePasswordUseCase {

    override suspend fun invoke(username: String, password: String) {
        val request = CreatePasswordRequest(
            id = username,
            password = password
        )

        enqueueActivityContextJobUseCase(
            request = { activity ->
                val credentialManager = CredentialManager.create(activity)
                try {
                    credentialManager.createCredential(
                        context = activity,
                        request = request
                    )

                    Log.i(SavePasswordUseCase.LOGTAG, "Save password for ${request.id} successful")
                } catch (_: CreateCredentialNoCreateOptionException) {
                    Log.w(SavePasswordUseCase.LOGTAG, "No option to create credentials e.g. no password manager installed")
                } catch (e: CreateCredentialException) {
                    Log.e(SavePasswordUseCase.LOGTAG, "Error saving credentials ${e.message}", e)
                } catch (t: Throwable) {
                    Log.e(SavePasswordUseCase.LOGTAG, "Unexpected error saving credentials", t)
                }
            }
        )
    }
}
