package world.respect.images

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import world.respect.appcompose.R


private val nameMap = mapOf(
    RespectImage.SPIX_LOGO to R.drawable.spix_logo,
    RespectImage.DIGITAL_LIBRARY to R.drawable.digital_library,
    RespectImage.WORKS_OFFLINE to R.drawable.works_offline,
    RespectImage.DATA_REPORTING to R.drawable.data_reporting,
    RespectImage.ASSIGNMENTS to R.drawable.assignments
    )

@Composable
actual fun respectImagePainter(image: RespectImage): Painter {
    return painterResource(nameMap[image] ?: throw IllegalArgumentException("no image for $image"))

}