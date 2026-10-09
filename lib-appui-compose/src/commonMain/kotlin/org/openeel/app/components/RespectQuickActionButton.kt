package world.respect.app.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun RespectQuickActionButton(
    labelText: String,
    modifier: Modifier = Modifier,
    iconContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val interactionSource =  remember { MutableInteractionSource() }

    TextButton(
        modifier = modifier.width(112.dp),
        onClick = onClick,
        interactionSource = interactionSource,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FilledTonalIconButton(
                onClick = onClick,
                interactionSource = interactionSource,
                enabled = enabled,
            ) {
                iconContent?.invoke()
            }

            Text(
                text = labelText,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.contentColorFor(MaterialTheme.colorScheme.background),
            )
        }
    }
}

@Composable
fun RespectQuickActionButton(
    imageVector: ImageVector? = null,
    labelText: String,
    enabled: Boolean = true,
    onClick: (() -> Unit) = {  },
){
    RespectQuickActionButton(
        iconContent = {
            if (imageVector != null) {
                Icon(imageVector = imageVector, contentDescription = null)
            }
        },
        labelText = labelText,
        onClick = onClick,
        enabled = enabled,
    )
}