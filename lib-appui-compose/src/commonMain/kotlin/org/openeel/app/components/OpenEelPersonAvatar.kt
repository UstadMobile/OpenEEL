package org.openeel.app.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ustadmobile.libuicompose.org.openeel.app.components.OpenEelAvatarBox
import org.openeel.shared.util.initial

@Composable
fun OpenEelPersonAvatar(
    name: String,
    modifier: Modifier = Modifier.defaultAvatarSize(),
    fontScale: Float = 1.0f,
) {
    OpenEelAvatarBox(
        modifier = modifier,
        name = name,
    ) {
        Text(
            style = MaterialTheme.typography.titleMedium.copy(
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = MaterialTheme.typography.titleMedium.fontSize * fontScale
            ),
            text=  name.initial(),
        )
    }
}
