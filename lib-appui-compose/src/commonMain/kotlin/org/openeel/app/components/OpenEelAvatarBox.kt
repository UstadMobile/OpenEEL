package com.ustadmobile.libuicompose.org.openeel.app.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import org.openeel.app.util.ext.rgbaColor
import org.openeel.shared.util.avatarColorForName

/**
 *
 */
@Composable
fun OpenEelAvatarBox(
    name: String,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val bgColor = avatarColorForName(name).rgbaColor()
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(SolidColor(bgColor))
        }

        content()
    }
}