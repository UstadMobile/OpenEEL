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
 *
 *
 *
 * Wraps an InputStream so that available() reports the number of bytes remaining according to the
 * known content length. Android's WebView (e.g. for media playback) can rely on available().
 *
 * See: https://issues.chromium.org/issues/40739128#comment24
 *  if the cache does not have it, then can return shouldIntercept=false.
 *
 * https://issuetracker.google.com/issues/283693355?pli=1
 *
 * See also: https://github.com/tauri-apps/wry/issues/1864
 *
 * https://github.com/apache/cordova-android/issues/1494#issuecomment-2982796417
 *
 * If contentLength is negative (unknown), available() delegates to the wrapped stream.
 */
class WebViewClientWorkaroundInputStream(
    inputStream: InputStream,
    private val headers: Map<String, String>,
    private val statusCode: Int,
): FilterInputStream(inputStream) {

    private val contentLength = if(statusCode == 206) {
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
            //logPartial(LOGTAG, "read")
            if(it != -1)
                bytesReadOrSkipped++
        }
    }

    override fun read(b: ByteArray): Int {
        return read(b, 0, b.size).also {
            logPartial(LOGTAG, "read $it bytes")
            if(it > 0)
                bytesReadOrSkipped += it
        }
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        return super.read(b, off, len).also {
            logPartial(LOGTAG, "read $it bytes")
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
