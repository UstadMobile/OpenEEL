package world.respect.app.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.assign_qr_code_badge
import world.respect.shared.generated.resources.change_qr_code_badge
import world.respect.shared.generated.resources.learn_more
import world.respect.shared.generated.resources.qr_code_badge
import world.respect.shared.generated.resources.qr_code_badge_description
import world.respect.shared.generated.resources.quick_easy_sign_in
import world.respect.shared.generated.resources.undraw_qr_code_scan_bewe


@Composable
fun RespectQrBadgeInfoBox(
    onClickLearnMore: () -> Unit,
    onClickAssignQrCodeBadge: () -> Unit,
    modifier: Modifier,
    isQrBadgeSet: Boolean? = null
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Image(
                    painter = painterResource(Res.drawable.undraw_qr_code_scan_bewe),
                    contentDescription = stringResource(Res.string.qr_code_badge),
                    modifier = Modifier
                        .size(100.dp, 120.dp)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.quick_easy_sign_in),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = stringResource(Res.string.qr_code_badge_description),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextButton(
                        onClick = onClickLearnMore,
                    ) {
                        Text(stringResource(Res.string.learn_more))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onClickAssignQrCodeBadge,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Text(
                    if (isQrBadgeSet == true) {
                        stringResource(Res.string.change_qr_code_badge)
                    } else {
                        stringResource(Res.string.assign_qr_code_badge)
                    }
                )
            }
        }
    }
}