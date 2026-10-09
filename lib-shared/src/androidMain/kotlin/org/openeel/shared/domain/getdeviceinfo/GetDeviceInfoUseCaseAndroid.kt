package world.respect.shared.domain.getdeviceinfo

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import io.github.aakira.napier.Napier
import world.respect.datalayer.school.model.DeviceInfo

class GetDeviceInfoUseCaseAndroid(
    context: Context
) : GetDeviceInfoUseCase {

    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    override fun invoke() : DeviceInfo {
        val totalMem = try {
            ActivityManager.MemoryInfo().also {
                activityManager.getMemoryInfo(it)
            }.totalMem
        }catch (e: Throwable) {
            Napier.w("Unable to get total memory", e)
            0
        }

        return DeviceInfo(
            platform = DeviceInfo.Platform.ANDROID,
            version = Build.VERSION.RELEASE,
            androidSdkInt = Build.VERSION.SDK_INT,
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            ram = totalMem,
        )
    }
}