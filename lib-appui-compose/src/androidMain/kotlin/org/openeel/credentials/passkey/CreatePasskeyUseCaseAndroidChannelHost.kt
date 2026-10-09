package world.respect.credentials.passkey

import kotlinx.coroutines.channels.Channel
import world.respect.credentials.passkey.CreatePasskeyUseCaseAndroidImpl.CreatePublicKeyCredentialRequestJob

class CreatePasskeyUseCaseAndroidChannelHost() {

    val requestChannel = Channel<CreatePublicKeyCredentialRequestJob>(capacity = Channel.RENDEZVOUS)

    suspend fun send(job: CreatePublicKeyCredentialRequestJob) {
        requestChannel.send(job)
    }

}