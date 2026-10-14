package org.openeel.images

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

enum class OpenEelImage {
    SPIX_LOGO,
    DIGITAL_LIBRARY,
    WORKS_OFFLINE,
    DATA_REPORTING,
    ASSIGNMENTS
}
@Composable
expect fun respectImagePainter(image: OpenEelImage): Painter