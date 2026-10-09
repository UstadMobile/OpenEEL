package world.respect.app.util.scanqrcode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
private class AndroidCameraPermissionState(
    private val permissionState: PermissionState,
) : CameraPermissionState {

    override val status: PermissionStatus
        get() = when (permissionState.status) {
            com.google.accompanist.permissions.PermissionStatus.Granted -> PermissionStatus.Granted
            is com.google.accompanist.permissions.PermissionStatus.Denied -> {
                val deniedStatus =
                    permissionState.status as com.google.accompanist.permissions.PermissionStatus.Denied
                PermissionStatus.Denied(deniedStatus.shouldShowRationale)
            }
        }

    override fun launchPermissionRequest() {
        permissionState.launchPermissionRequest()
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun rememberCameraPermissionState(): CameraPermissionState {
    val permissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )
    return remember {
        AndroidCameraPermissionState(permissionState)
    }
}