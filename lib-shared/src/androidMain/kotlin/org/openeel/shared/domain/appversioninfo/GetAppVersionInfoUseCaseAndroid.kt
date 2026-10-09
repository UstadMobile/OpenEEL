package world.respect.shared.domain.appversioninfo

import android.content.Context
import android.os.Build
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import world.respect.shared.generated.resources.Res
import java.io.ByteArrayInputStream
import java.util.Properties

class GetAppVersionInfoUseCaseAndroid(
    private val context: Context
) : GetAppVersionInfoUseCase{

    private val buildInfoProperties = Properties()

    override suspend fun invoke(): GetAppVersionInfoUseCase.AppVersionInfo {
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName, 0
        )

        val versionCode = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode.toInt()
        }else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode
        }

        if(buildInfoProperties.isEmpty) {
            withContext(Dispatchers.IO) {
                try {
                    buildInfoProperties.load(
                        ByteArrayInputStream(
                            Res.readBytes("files/buildinfo.properties")
                        )
                    )
                }catch(t: Throwable) {
                    Napier.w("Failed to load build info properties", t)
                    buildInfoProperties.setProperty("loadfailed", "dontloadagain")
                }
            }
        }

        return GetAppVersionInfoUseCase.AppVersionInfo(
            version = packageInfo.versionName ?: "",
            versionCode = versionCode,
            packageName = context.packageName,
            buildTag = buildInfoProperties.getProperty("buildtag"),
            buildTime = buildInfoProperties.getProperty("buildtime"),
        )
    }
}