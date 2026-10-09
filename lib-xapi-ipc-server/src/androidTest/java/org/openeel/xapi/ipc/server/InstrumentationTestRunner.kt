package world.respect.xapi.ipc.server

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

/**
 * As per: https://insert-koin.io/docs/reference/koin-android/instrumented-testing/#strategy-1-custom-test-application
 */
class InstrumentationTestRunner: AndroidJUnitRunner() {

    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application? {
        return super.newApplication(cl, IpcTestApplication::class.java.name, context)
    }
}