package world.respect.app.view.acknowledgement

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.app.AppIcon
import world.respect.shared.viewmodel.acknowledgement.AcknowledgementUiState
import world.respect.shared.viewmodel.acknowledgement.AcknowledgementViewModel
import world.respect.images.RespectImage
import world.respect.images.respectImagePainter
import world.respect.shared.generated.resources.*

@Composable
fun AcknowledgementScreen(
    viewModel: AcknowledgementViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    AcknowledgementScreen(uiState)
}

@Composable
fun AcknowledgementScreen(
    uiState: AcknowledgementUiState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AppIcon()

        Spacer(Modifier.height(48.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(Res.string.supported_by)
            )
            Spacer(Modifier.height(8.dp))
            Image(
                painter =  respectImagePainter(RespectImage.SPIX_LOGO),
                contentDescription = "Supported by",
                modifier = Modifier
                    .size(120.dp)
            )
        }

        Spacer(Modifier.height(64.dp))


        Text(stringResource(Res.string.developed_by))
    }
}
