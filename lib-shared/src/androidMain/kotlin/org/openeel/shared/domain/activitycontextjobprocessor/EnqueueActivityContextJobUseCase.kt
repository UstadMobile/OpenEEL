package world.respect.shared.domain.activitycontextjobprocessor

import kotlinx.coroutines.channels.Channel

/**
 * There are various components and use cases in Android where only an activity context can be used.
 *
 * The dependency injection ViewModels and other components are hosted by the Application, not the
 * activity.
 *
 * The workaround here is for the application to host a channel of jobs that can then be processed
 * by the ActivityContextJobProcessor
 */
class EnqueueActivityContextJobUseCase {

    val jobChannel = Channel<ActivityContextJob>(Channel.UNLIMITED)

    suspend operator fun invoke(request: ActivityContextJob) {
        jobChannel.send(request)
    }
}