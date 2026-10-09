package world.respect.app.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.koin.compose.getKoin
import world.respect.shared.domain.appversioninfo.GetAppVersionInfoUseCase

@Composable
fun rememberAppVersionInfo(): GetAppVersionInfoUseCase.AppVersionInfo? {
    val koin = getKoin()

    val getVersionInfo = remember(Unit) {
        koin.get<GetAppVersionInfoUseCase>()
    }

    var versionInfo by remember {
        mutableStateOf<GetAppVersionInfoUseCase.AppVersionInfo?>(null)
    }

    LaunchedEffect(Unit) {
        versionInfo = getVersionInfo()
    }

    return versionInfo
}