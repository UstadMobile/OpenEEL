package world.respect.datalayer.repository.school.xapi

import kotlinx.coroutines.channels.Channel
import world.respect.datalayer.school.writequeue.EnqueueDrainRemoteWriteQueueUseCase

/**
 * The normal EnqueueDrainRemoteWriteQueueUseCase uses WorkManager on Android.
 * ChannelRemoteWriteQueue uses a simple in-memory channel to trigger DrainRemoteWriteQueueUseCase
 * for testing purposes.
 */
class EnqueueDrainRemoteWriteQueueUseCaseChannel(

): EnqueueDrainRemoteWriteQueueUseCase {

    val channel = Channel<Boolean>(capacity = Channel.UNLIMITED)

    override suspend fun invoke() {
        channel.send(true)
    }

}