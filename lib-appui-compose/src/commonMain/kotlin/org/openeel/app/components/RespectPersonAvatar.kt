package world.respect.app.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import world.respect.app.util.ext.rgbaColor
import world.respect.shared.util.avatarColorForName
import world.respect.shared.util.initial

@Composable
fun RespectPersonAvatar(
    name: String,
    modifier: Modifier = Modifier.defaultAvatarSize(),
    fontScale: Float = 1.0f,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val bgColor = avatarColorForName(name).rgbaColor()
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(SolidColor(bgColor))
        }

        Text(
            style = MaterialTheme.typography.titleMedium.copy(
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = MaterialTheme.typography.titleMedium.fontSize * fontScale
            ),
            text=  name.initial(),
        )
    }
}
