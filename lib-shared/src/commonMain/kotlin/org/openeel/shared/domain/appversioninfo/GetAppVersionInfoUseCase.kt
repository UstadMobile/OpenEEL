package world.respect.shared.domain.appversioninfo

fun interface GetAppVersionInfoUseCase {

    data class AppVersionInfo(
        val version: String,
        val versionCode: Int,
        val packageName: String,
        val buildTag: String?,
        val buildTime: String?,
    )

    suspend operator fun invoke(): AppVersionInfo

}