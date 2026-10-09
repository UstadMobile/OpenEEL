package world.respect.domain.account.passkey

import io.ktor.http.Url
import junit.framework.TestCase
import world.respect.credentials.passkey.RespectUserHandle
import world.respect.shared.domain.account.passkey.DecodeUserHandleUseCaseImpl
import world.respect.shared.domain.account.passkey.EncodeUserHandleUseCaseImpl
import kotlin.random.Random
import kotlin.test.Test

class EncodeDecodeUserHandleUseCaseTest {

    @Test
    fun givenPersonUidAndLearningSpace_whenEncodedAndThenDecoded_thenShouldReturnSameValues() {
        val userHandle = RespectUserHandle(
            Random.Default.nextLong(0, Long.MAX_VALUE),
            Url("http://localhost/sub/")
        )

        val encodedStr  = EncodeUserHandleUseCaseImpl().invoke(userHandle)
        val decodedHandle = DecodeUserHandleUseCaseImpl().invoke(encodedStr)
        TestCase.assertEquals(userHandle, decodedHandle)
    }

}