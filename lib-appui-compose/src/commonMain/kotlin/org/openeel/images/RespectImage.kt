package world.respect.images

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

enum class RespectImage {
    SPIX_LOGO,
    DIGITAL_LIBRARY,
    WORKS_OFFLINE,
    DATA_REPORTING,
    ASSIGNMENTS
}
@Composable
expect fun respectImagePainter(image: RespectImage): Painter