package world.respect.app.view.person.qrcode

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.defaultItemPadding
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.qr_code_private_screen
import world.respect.shared.viewmodel.person.qrcode.InviteQrUiState
import world.respect.shared.viewmodel.person.qrcode.InviteQrViewModel

@Composable
fun InviteQrScreen(viewModel: InviteQrViewModel) {
    val uiState by viewModel.uiState.collectAsState(Dispatchers.Main.immediate)

    InviteQrScreen(
        uiState = uiState,
    )
}

@Composable
fun InviteQrScreen(
    uiState: InviteQrUiState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .defaultItemPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = uiState.schoolOrClass ?: "",
            modifier = Modifier.padding(vertical = 12.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))
        val link = uiState.link
        if (link != null) {
            Image(
                painter = rememberQrCodePainter(link),
                contentDescription = "QR Code",
                modifier = Modifier.size(400.dp).padding(horizontal = 50.dp)
            )
            Text(
                text = link,
                modifier = Modifier
                    .testTag("invite_qr_link") ,
                color = Color.Transparent
            )
        }


        Text(
            text = stringResource(Res.string.qr_code_private_screen),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
