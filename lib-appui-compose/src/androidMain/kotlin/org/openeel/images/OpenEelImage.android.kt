package org.openeel.images

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import org.openeel.appcompose.R


private val nameMap = mapOf(
    OpenEelImage.SPIX_LOGO to R.drawable.spix_logo,
    OpenEelImage.DIGITAL_LIBRARY to R.drawable.digital_library,
    OpenEelImage.WORKS_OFFLINE to R.drawable.works_offline,
    OpenEelImage.DATA_REPORTING to R.drawable.data_reporting,
    OpenEelImage.ASSIGNMENTS to R.drawable.assignments
    )

@Composable
actual fun respectImagePainter(image: OpenEelImage): Painter {
    return painterResource(nameMap[image] ?: throw IllegalArgumentException("no image for $image"))

}