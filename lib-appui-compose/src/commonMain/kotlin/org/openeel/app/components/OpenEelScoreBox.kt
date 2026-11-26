package org.openeel.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

/**
 * Shows a scaled score (0 to 1) as a percentage. The background is green if [successful] is true,
 * red if it is false, and transparent if it is null.
 */
@Composable
fun OpenEelScoreBox(
    scoreScaled: Float,
    successful: Boolean?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(
            color = when (successful) {
                true -> Color.Green
                false -> Color.Red
                null -> Color.Transparent
            }
        ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "${(scoreScaled * 100).roundToInt()}%",
            color = if(successful != null) Color.White else Color.Unspecified,
        )
    }
}
