package world.respect.credentials.passkey

import android.app.Activity
import androidx.credentials.CredentialManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/**
 *
 * @param activityContext ActivityContext to use with CredentialManager
 * @param channel Channel to receive requests on
 * @param processOnScope CoroutineScope to process requests on. ReceiveJobs will run on using
 *        repeatOnLifecycle to prevent an inactive activity from processing requests. ReceiveJobs
 *        itself will hence be canceled when the activity is in the background (e.g. when the user
 *        is prompted for a passkey). ProcessOnScope will be used to run a job once received.
 */
class GetCredentialUseCaseProcessor(
    private val activityContext: Activity,
    private val channel: Channel<GetCredentialUseCaseAndroidImpl.GetCredentialJob>,
    private val processOnScope: CoroutineScope,
) {

    val credentialManager = CredentialManager.create(activityContext)

    suspend fun receiveJobs() {
        for (job in channel) {
            processOnScope.launch {
                try {
                    job.response.complete(
                        credentialManager.getCredential(
                            context = activityContext,
                            request = job.request
                        )
                    )
                }catch (e: Throwable) {
                    job.response.completeExceptionally(e)
                }
            }
        }
    }

}