package world.respect.app.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.ktor.http.Url

@Composable
expect fun BasicWebView(
    url: Url,
    modifier: Modifier = Modifier,
)