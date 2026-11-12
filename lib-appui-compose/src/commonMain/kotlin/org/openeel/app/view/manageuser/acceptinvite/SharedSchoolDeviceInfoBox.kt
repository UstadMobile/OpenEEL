package org.openeel.app.view.manageuser.acceptinvite

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.enable
import org.openeel.shared.generated.resources.image_shared_device
import org.openeel.shared.generated.resources.shared_device
import org.openeel.shared.generated.resources.shared_device_description_1
import org.openeel.shared.generated.resources.shared_device_description_2
import org.openeel.shared.generated.resources.shared_device_description_3
import org.openeel.shared.generated.resources.undraw_sync_pe2t_1


@Composable
fun SharedSchoolDeviceInfoBox(
    onClickEnableSharedSchoolDeviceMode: () -> Unit,
    modifier: Modifier = Modifier,
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
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = painterResource(Res.drawable.undraw_sync_pe2t_1),
                    contentDescription = stringResource(Res.string.image_shared_device),
                    modifier = Modifier
                        .width(120.dp)
                        .height(100.dp)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.shared_device),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = " * ${stringResource(Res.string.shared_device_description_1)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = " * ${stringResource(Res.string.shared_device_description_2)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = " * ${stringResource(Res.string.shared_device_description_3)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Button(
                onClick = onClickEnableSharedSchoolDeviceMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("enable_button"),
                enabled = true,
            ) {
                Text(stringResource(Res.string.enable))
            }
        }
    }
}
