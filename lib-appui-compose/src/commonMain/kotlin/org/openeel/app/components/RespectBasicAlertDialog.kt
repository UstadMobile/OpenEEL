package world.respect.app.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.confirm
import world.respect.shared.generated.resources.dismiss

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RespectBasicAlertDialog(
    headlineText: String? = null,
    bodyText: String,
    onConfirm: (() -> Unit),
    onDismissRequest: () -> Unit,
    confirmText: String? = stringResource(Res.string.confirm),
    dismissText: String? = stringResource(Res.string.dismiss),
) {
    //As per https://kotlinlang.org/api/compose-multiplatform/material3/androidx.compose.material3/-basic-alert-dialog.html
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(),
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 300.dp).wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                headlineText?.also {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = bodyText,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(24.dp))

                if(confirmText != null || dismissText != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        confirmText?.also {
                            TextButton(onClick = onConfirm) {
                                Text(it)
                            }
                        }

                        dismissText?.also {
                            TextButton(onClick = onDismissRequest) {
                                Text(it)
                            }
                        }
                    }
                }
            }
        }

    }
}