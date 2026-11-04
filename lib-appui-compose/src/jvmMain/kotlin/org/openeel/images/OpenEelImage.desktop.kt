package org.openeel.images

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource


val nameMap = mapOf(
    OpenEelImage.SPIX_LOGO to "/img/spix_logo.png",
)
@Composable
actual fun respectImagePainter(image: OpenEelImage): Painter {
    return painterResource(nameMap[image]  ?: throw IllegalArgumentException("no image for $image"))
}