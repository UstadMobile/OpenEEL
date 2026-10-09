package world.respect.shared.domain.validator

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.remaining
import io.ktor.utils.io.exhausted
import io.ktor.utils.io.readRemaining
import kotlinx.io.asSink

suspend fun HttpResponse.readAndDiscard() {
    if(status.value != 204 && status.value !in 300..399) {
        val sink = DiscardOutputStream().asSink()
        val channel: ByteReadChannel = body()
        var count = 0L
        while(!channel.exhausted()) {
            val chunk = channel.readRemaining()
            count += chunk.remaining
            chunk.transferTo(sink)
        }
    }
}
