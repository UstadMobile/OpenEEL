package world.respect.app.view.scanqrcode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.ncgroup.kscan.BarcodeFormat
import org.ncgroup.kscan.BarcodeResult
import org.ncgroup.kscan.ScannerView
import world.respect.app.util.scanqrcode.PermissionStatus
import world.respect.app.util.scanqrcode.rememberCameraPermissionState
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.camera_access_required
import world.respect.shared.generated.resources.camera_permission
import world.respect.shared.generated.resources.cancel
import world.respect.shared.generated.resources.close
import world.respect.shared.generated.resources.ok
import world.respect.shared.generated.resources.paste_url
import world.respect.shared.generated.resources.qr_code_invalid_format
import world.respect.shared.generated.resources.request_permission
import world.respect.shared.generated.resources.try_again
import world.respect.shared.generated.resources.url
import world.respect.shared.viewmodel.scanqrcode.ScanQRCodeUiState
import world.respect.shared.viewmodel.scanqrcode.ScanQRCodeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanQRCodeScreen(
    viewModel: ScanQRCodeViewModel
) {
    val uiState: ScanQRCodeUiState by viewModel.uiState.collectAsState(
        ScanQRCodeUiState()
    )
    val coroutineScope = rememberCoroutineScope()
    var showScanner by remember { mutableStateOf(true) }

    val cameraPermissionState = rememberCameraPermissionState()
    var hasDeniedOnce by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (cameraPermissionState.status is PermissionStatus.Denied) {
            cameraPermissionState.launchPermissionRequest()
            hasDeniedOnce = true
        }
    }
    when (cameraPermissionState.status) {
        PermissionStatus.Granted -> {
            when {
                // Show error screen if there's an error
                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            modifier = Modifier.size(100.dp).padding(bottom = 10.dp),
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(Res.string.qr_code_invalid_format),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 16.dp),
                        )

                        Button(
                            onClick = viewModel::onClickTryAgain,
                            modifier = Modifier.fillMaxWidth(0.7f),
                        ) {
                            Text(
                                text = stringResource(Res.string.try_again),
                            )
                        }
                    }
                }

                // Show scanner view if no error and showScanner enabled
                !uiState.showManualEntryDialog && showScanner -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        ScannerView(
                            codeTypes = listOf(
                                BarcodeFormat.FORMAT_QR_CODE,
                            ),
                            scannerUiOptions = null,
                            modifier = Modifier.fillMaxSize()
                        ) { result ->
                            when (result) {
                                is BarcodeResult.OnSuccess -> {
                                    viewModel.onQrCodeScanned(result.barcode.data)
                                    showScanner = false
                                }

                                is BarcodeResult.OnFailed -> {
                                    showScanner = false
                                    viewModel.onQrCodeScanError(result.exception)
                                }

                                BarcodeResult.OnCanceled -> {
                                    showScanner = false
                                }
                            }
                        }

                        ScannerOverlay()
                    }
                }
            }

            // Manual URL Entry Dialog
            if (uiState.showManualEntryDialog) {
                var manualUrlText by remember { mutableStateOf(TextFieldValue("")) }
                ManualUrlEntryDialog(
                    manualUrlText = manualUrlText,
                    onUrlTextChange = { manualUrlText = it },
                    onDismiss = {
                        viewModel.hideManualEntryDialog()
                        manualUrlText = TextFieldValue("")
                        showScanner = true
                    },
                    onSubmit = { url ->
                        if (url.isNotEmpty()) {
                            coroutineScope.launch {
                                viewModel.onQrCodeScanned(url)
                                viewModel.hideManualEntryDialog()
                            }
                        }
                    },
                )
            }
        }

        is PermissionStatus.Denied -> {
            val deniedStatus = cameraPermissionState.status as PermissionStatus.Denied
            if (deniedStatus.shouldShowRationale) {
                PermissionDeniedScreen(
                    onGrandPermission = {
                        cameraPermissionState.launchPermissionRequest()
                    },
                )
            } else {
                if (hasDeniedOnce) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(Res.string.camera_permission),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionDeniedScreen(
    onGrandPermission: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            modifier = Modifier.size(100.dp).padding(bottom = 16.dp),
            imageVector = Icons.Outlined.CameraAlt,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = stringResource(Res.string.camera_access_required),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Button(
            onClick = { onGrandPermission() },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(
                text = stringResource(Res.string.request_permission),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

}

@Composable
fun ScannerOverlay() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
        )
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.Center)
                .background(Color.Transparent)
        ) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawRect(
                    color = Color.White,
                    topLeft = Offset.Zero,
                    size = size,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManualUrlEntryDialog(
    manualUrlText: TextFieldValue,
    onUrlTextChange: (TextFieldValue) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.extraLarge
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.paste_url),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(Res.string.close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value = manualUrlText,
                onValueChange = { newValue ->
                    onUrlTextChange(newValue)
                },
                label = {
                    Text(stringResource(Res.string.url))
                },
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
                singleLine = true,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.width(100.dp)
                ) {
                    Text(stringResource(Res.string.cancel))
                }

                Spacer(modifier = Modifier.width(16.dp))

                TextButton(
                    onClick = {
                        val url = manualUrlText.text.trim()
                        onSubmit(url)
                    },
                    modifier = Modifier.width(100.dp),
                    enabled = manualUrlText.text.isNotBlank()
                ) {
                    Text(stringResource(Res.string.ok))
                }
            }
        }
    }
}
