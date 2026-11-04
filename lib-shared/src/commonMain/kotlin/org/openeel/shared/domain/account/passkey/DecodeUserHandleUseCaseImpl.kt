package org.openeel.shared.domain.account.passkey

import io.ktor.http.Url
import org.openeel.credentials.passkey.RespectUserHandle
import org.openeel.credentials.passkey.request.DecodeUserHandleUseCase
import org.openeel.shared.util.base64StringToByteArray
import java.nio.ByteBuffer
import java.nio.ByteOrder


class DecodeUserHandleUseCaseImpl : DecodeUserHandleUseCase {

    override operator fun invoke(
        encodedHandle: String
    ): RespectUserHandle {
        val decodedBytes = encodedHandle.base64StringToByteArray()
        val byteBuffer = ByteBuffer.wrap(decodedBytes)
        byteBuffer.order(ByteOrder.BIG_ENDIAN)
        val personUid = byteBuffer.long
        val urlLen = byteBuffer.get()
        val urlBytes = ByteArray(urlLen.toInt())
        byteBuffer.get(urlBytes)
        val schoolUrl = urlBytes.decodeToString()

        return RespectUserHandle(
            personUidNum = personUid,
            schoolUrl = Url(schoolUrl),
        )
    }

}
