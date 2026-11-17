package com.ustadmobile.libcache.webview

import android.util.Log
import io.ktor.http.HttpHeaders
import java.io.FilterInputStream
import java.io.InputStream

private fun Map<String, String>.getCaseInsensitive(
    key: String
): String? {
    return get(key) ?: entries.firstOrNull { it.key.equals(key, true) }?.value
}

/**
 * FilterInputStream that works around issues in [android.webkit.WebViewClient.shouldInterceptRequest]
 * used together with [WebResourceResponseWorkaround].
 *
 * Wraps an InputStream so that available() reports the number of bytes remaining according to the
 * known total content length. Android's WebView (e.g. for media playback) can rely on available().
 *
 * If contentLength is negative (unknown), available() delegates to the wrapped stream.
 */
class WebViewClientWorkaroundInputStream(
    inputStream: InputStream,
    headers: Map<String, String>,
    private val statusCode: Int,
): FilterInputStream(inputStream) {

    /**
     * Content-Length needs to be based on the full length of the resource.
     */
    private val contentLength = if(statusCode == 206) {
        /*
         * When serving a partial response, we need to use the content range header, which comes in
         * the form of "Content-Range: from-to/total"
         */
        headers.getCaseInsensitive("Content-Range")?.substringAfterLast("Content-Range")
            ?.substringAfterLast("/")?.toLong() ?: -1
    }else {
        headers.getCaseInsensitive(HttpHeaders.ContentLength)?.toLong() ?: -1
    }

    private var bytesReadOrSkipped = 0L
    
    private fun logPartial(
        tag: String,
        message: String
    ) {
        if(statusCode == 206)
            Log.d(tag, message)
    }

    override fun read(): Int {
        return super.read().also {
            if(it != -1)
                bytesReadOrSkipped++
        }
    }

    override fun read(b: ByteArray): Int {
        return read(b, 0, b.size).also {
            if(it > 0)
                bytesReadOrSkipped += it
        }
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        return super.read(b, off, len).also {
            if(it > 0)
                bytesReadOrSkipped += it
        }
    }

    /**
     * Skip is being used incorrectly as per issue references: so we need to skip the skip for
     * the correct data to be delivered.
     */
    override fun skip(n: Long): Long {
        return if(statusCode == 206) {
            bytesReadOrSkipped += n
            logPartial(LOGTAG, "skip request $n")
            n
        }else {
            super.skip(n).also {
                logPartial(LOGTAG, "skip $it/request $n")
                if (it > 0) {
                    bytesReadOrSkipped += it
                }
            }
        }
    }

    override fun markSupported(): Boolean {
        logPartial(LOGTAG, "markSupported")
        return false
    }

    override fun mark(readlimit: Int) {
        logPartial(LOGTAG, "mark $readlimit")
        //Do nothing - mark/reset is not supported
    }

    override fun reset() {
        logPartial(LOGTAG, "reset")
        throw java.io.IOException("mark/reset not supported")
    }

    override fun available(): Int {
        return if(contentLength >= 0) {
            (contentLength - bytesReadOrSkipped).coerceIn(0L, Int.MAX_VALUE.toLong()).toInt().also {
                logPartial(LOGTAG, "available: $it bytes")
            }
        }else {
            super.available().also {
                logPartial(LOGTAG, "super.available: $it bytes")
            }
        }
    }

    override fun close() {
        logPartial(LOGTAG, "close")
        super.close()
    }

    companion object {

        const val LOGTAG = "ContentLengthInputStreamWrapper"

    }
}
