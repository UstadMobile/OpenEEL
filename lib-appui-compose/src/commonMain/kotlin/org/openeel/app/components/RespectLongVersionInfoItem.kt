package world.respect.app.components

import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.app_version

@Composable
fun RespectLongVersionInfoItem(
    modifier: Modifier = Modifier,
    onDevModeEnabled: () -> Unit = { },
) {
    val versionInfo = rememberAppVersionInfo()

    if(versionInfo != null) {
        ListItem(
            modifier = modifier.clickableToEnableDevMode(onDevModeEnabled = onDevModeEnabled),
            headlineContent = {
                Text("${stringResource(Res.string.app_version)}: ${versionInfo.version} (#${versionInfo.versionCode})")
            },
            supportingContent = {
                if(versionInfo.buildTag != null || versionInfo.buildTime != null) {
                    Text(
                        text = "${versionInfo.buildTag ?: ""} (${versionInfo.buildTime ?: ""})"
                    )
                }
            }
        )
    }
}