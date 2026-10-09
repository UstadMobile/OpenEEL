package world.respect.xapi.ipc.shared.messages.ext

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import world.respect.lib.xapi.model.XapiDocumentByteArrayImpl
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)
class XapiDocumentAdapterTest {

    private lateinit var executorService: ExecutorService

    @Before
    fun setup() {
        executorService = Executors.newCachedThreadPool()
    }

    @After
    fun tearDown() {
        executorService.shutdown()
    }

    @Test
    fun givenConvertedToFromBundleThenShouldMatch() = runBlocking {
        val document = XapiDocumentByteArrayImpl(
            type = "application/json",
            updated = GMTDate(1700000000000L),
            contents = """{"test":"hello world"}""".encodeToByteArray()
        )

        val bundle = document.toBundle(executorService)
        val documentFromBundle = bundle.toXapiDocument()

        Assert.assertEquals(document.type, documentFromBundle.type)
        Assert.assertEquals(document.updated, documentFromBundle.updated)
        Assert.assertArrayEquals(document.contentsAsByteArray(), documentFromBundle.contentsAsByteArray())
        // Verify multiple reads succeed
        Assert.assertArrayEquals(document.contentsAsByteArray(), documentFromBundle.contentsAsByteArray())
    }

}
