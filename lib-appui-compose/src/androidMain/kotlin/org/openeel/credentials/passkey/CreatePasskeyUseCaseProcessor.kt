package world.respect.credentials.passkey

import android.app.Activity
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.CredentialManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/**
 * Receive requests to create a passkey from the CreatePasskeyUseCase and process them using an
 * activity context.
 *
 * @param activityContext ActivityContext to use with CredentialManager
 * @param jobChannel Channel to receive requests on
 * @param processOnScope CoroutineScope to process requests on. ReceiveJobs will run on using
 *        repeatOnLifecycle to prevent an inactive activity from processing requests. ReceiveJobs
 *        itself will hence be canceled when the activity is in the background (e.g. when the user
 *        is prompted for a passkey). ProcessOnScope will be used to run a job once received.
 */
class CreatePasskeyUseCaseProcessor(
    private val activityContext: Activity,
    private val jobChannel: Channel<CreatePasskeyUseCaseAndroidImpl.CreatePublicKeyCredentialRequestJob>,
    private val processOnScope: CoroutineScope,
) {

    val credentialManager = CredentialManager.create(activityContext)

    /**
     * Receive jobs should be run from an activity using
     */
    suspend fun receiveJobs() {
        for (job in jobChannel) {
            processOnScope.launch {
                try {
                    val response = credentialManager.createCredential(
                        context = activityContext,
                        request = job.request
                    ) as CreatePublicKeyCredentialResponse
                    job.response.complete(response)
                }catch(e: Throwable) {
                    job.response.completeExceptionally(e)
                }
            }
        }
    }


}