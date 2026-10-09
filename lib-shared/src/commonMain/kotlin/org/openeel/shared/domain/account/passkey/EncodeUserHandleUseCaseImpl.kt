package world.respect.shared.domain.account.passkey

import io.ktor.util.encodeBase64
import world.respect.credentials.passkey.RespectUserHandle
import world.respect.credentials.passkey.request.EncodeUserHandleUseCase
import java.nio.ByteBuffer
import java.nio.ByteOrder


class EncodeUserHandleUseCaseImpl() : EncodeUserHandleUseCase {

    override fun invoke(
        userHandle: RespectUserHandle,
    ): String {
        val urlBytes = userHandle.schoolUrl.toString().encodeToByteArray()
        val buffer = ByteBuffer.allocate(Long.SIZE_BYTES + 1 + urlBytes.size)
        buffer.order(ByteOrder.BIG_ENDIAN)
        buffer.putLong(userHandle.personUidNum)
        buffer.put(urlBytes.size.toByte())
        buffer.put(urlBytes)

        return buffer.array().encodeBase64()
    }
}