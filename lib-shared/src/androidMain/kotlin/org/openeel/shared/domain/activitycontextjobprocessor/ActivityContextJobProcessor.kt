package world.respect.shared.domain.activitycontextjobprocessor

import android.app.Activity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/**
 * Processes jobs from SubmitActivityContextJobUseCase
 *
 * See SubmitActivityContextJobUseCase
 */
class ActivityContextJobProcessor(
    private val activityContext: Activity,
    private val jobChannel: Channel<ActivityContextJob>,
    private val processOnScope: CoroutineScope,
) {

    suspend fun receiveJobs() {
        for (job in jobChannel) {
            processOnScope.launch {
                job.doJob(activityContext)
            }
        }
    }

}