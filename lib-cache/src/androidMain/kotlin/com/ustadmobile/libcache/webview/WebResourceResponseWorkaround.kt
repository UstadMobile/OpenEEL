package com.ustadmobile.libcache.webview

import android.webkit.WebResourceResponse
import java.io.InputStream

class WebResourceResponseWorkaround(
    mimeType: String,
    encoding: String?,
    statusCode: Int,
    reasonPhrase: String,
    responseHeaders: Map<String, String>,
    data: InputStream,
): WebResourceResponse(
    mimeType,
    encoding,
    statusCode,
    reasonPhrase,
    responseHeaders.filter { headerEntry ->
        !WEBVIEW_GENERATED_RESPONSE_HEADERS.any { it.equals(headerEntry.key, true) }
    },
    WebViewClientWorkaroundInputStream(data, responseHeaders, statusCode),
) {
    companion object {

        val WEBVIEW_GENERATED_RESPONSE_HEADERS = listOf("Content-Type", "Content-Length", "Content-Encoding")

    }
}