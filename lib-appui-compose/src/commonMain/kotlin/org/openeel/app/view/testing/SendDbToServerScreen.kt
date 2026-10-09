package world.respect.app.view.testing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.send_db_upload_complete
import world.respect.shared.generated.resources.uploading

data class SendDbToServerUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@Composable
fun SendDbToServerScreen(uiState: SendDbToServerUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
                Text(
                    text = stringResource(Res.string.uploading),
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .testTag("db_progress"),
                )
            }

            /**
             * Even if the upload is failed: it is done. This is seen by e2e_artifact_upload.yaml.
             *
             * If the text assertion fails it will not proceed to call teardown.js which is needed
             * to stop the server.
             */
            uiState.errorMessage != null -> {
                Text(
                    text = uiState.errorMessage,
                    modifier = Modifier.testTag("upload_done"),
                )
            }

            else -> {
                Text(
                    text = stringResource(Res.string.send_db_upload_complete),
                    modifier = Modifier.testTag("upload_done"),
                )
            }
        }
    }
}
