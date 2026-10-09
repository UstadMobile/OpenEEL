package world.respect.app.util.scanqrcode

import androidx.compose.runtime.Composable

sealed class PermissionStatus {
    object Granted : PermissionStatus()
    data class Denied(val shouldShowRationale: Boolean) : PermissionStatus()
}

interface CameraPermissionState {
    val status: PermissionStatus
    fun launchPermissionRequest()
}

@Composable
expect fun rememberCameraPermissionState(): CameraPermissionState