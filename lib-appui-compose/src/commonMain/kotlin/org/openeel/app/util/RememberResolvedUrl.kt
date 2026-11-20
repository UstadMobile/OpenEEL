package org.openeel.app.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.ktor.http.Url
import org.openeel.libutil.ext.resolveSafely

@Composable
fun rememberResolvedHref(
    baseUrl: Url,
    href: String,
) : Url{
    return remember(baseUrl, href) {
        baseUrl.resolveSafely(href)
    }
}
