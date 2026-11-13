package com.ustadmobile.libcache.webview

import android.webkit.WebResourceResponse
import java.io.InputStream

/**
 * [android.webkit.WebViewClient.shouldInterceptRequest] handles range requests in a way that is
 * "quite broken/wrong" as per Google themselves:
 * https://issuetracker.google.com/issues/283693355#comment2
 *
 * Failure to support range requests properly results in media playback in the WebView failing.
 *
 * Bad behaviors include:
 *
 * - Using the [data].available to set the Content-Length header, even though (as per docs) an
 *   [InputStream.available] indicates how many bytes are available immediately, NOT the total
 *   length of the stream.
 * - Adding a Content-Length and Content-Type header even when those are in [responseHeaders].
 *   If these headers already exist, then a single header with the two values separated by a comma
 *   is set on the response (as per Chrome inspector).
 * - Using [InputStream.skip] to skip bytes specified on a range response. A server or cache that
 *   correctly handles a range request would have already skipped those bytes. Using the skip
 *   function again goes to the wrong position in the stream and results in playback failing.
 *
 * See also:
 * https://issues.chromium.org/issues/40739128#comment24
 * https://github.com/tauri-apps/wry/issues/1864
 * https://github.com/apache/cordova-android/issues/1494#issuecomment-2982796417
 *
 */
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