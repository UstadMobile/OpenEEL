package org.openeel.domain.account.passkey

import io.ktor.http.Url
import junit.framework.TestCase
import org.openeel.credentials.passkey.OpenEelUserHandle
import org.openeel.shared.domain.account.passkey.DecodeUserHandleUseCaseImpl
import org.openeel.shared.domain.account.passkey.EncodeUserHandleUseCaseImpl
import kotlin.random.Random
import kotlin.test.Test

class EncodeDecodeOpenEelUserHandleUseCaseTest {

    @Test
    fun givenPersonUidAndLearningSpace_whenEncodedAndThenDecoded_thenShouldReturnSameValues() {
        val userHandle = OpenEelUserHandle(
            Random.Default.nextLong(0, Long.MAX_VALUE),
            Url("http://localhost/sub/")
        )

        val encodedStr  = EncodeUserHandleUseCaseImpl().invoke(userHandle)
        val decodedHandle = DecodeUserHandleUseCaseImpl().invoke(encodedStr)
        TestCase.assertEquals(userHandle, decodedHandle)
    }

}