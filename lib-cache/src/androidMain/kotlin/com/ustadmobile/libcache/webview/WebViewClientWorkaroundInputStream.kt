package com.ustadmobile.libcache.webview

import android.util.Log
import java.io.FilterInputStream
import java.io.InputStream

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
    private val contentLength: Long,
): FilterInputStream(inputStream) {

    private var bytesReadOrSkipped = 0L

    override fun read(): Int {
        return super.read().also {
            //Log.d(LOGTAG, "read")
            if(it != -1)
                bytesReadOrSkipped++
        }
    }

    override fun read(b: ByteArray): Int {
        return read(b, 0, b.size).also {
            //Log.d(LOGTAG, "read $it bytes")
            if(it > 0)
                bytesReadOrSkipped += it
        }
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        return super.read(b, off, len).also {
            //Log.d(LOGTAG, "read $it bytes")
            if(it > 0)
                bytesReadOrSkipped += it
        }
    }

    /**
     * Skip is being used incorrectly as per issue references: so we need to skip the skip for
     * the correct data to be delivered.
     */
    override fun skip(n: Long): Long {
        //This is being handled VERY wrong
        Log.d(LOGTAG, "skip request $n")
        return n
//        return super.skip(n).also {
//            Log.d(LOGTAG, "skip $it/request $n")
//            if(it > 0)
//                bytesReadOrSkipped += it
//        }
    }

    override fun markSupported(): Boolean {
        Log.d(LOGTAG, "markSupported")
        return false
    }

    override fun mark(readlimit: Int) {
        Log.d(LOGTAG, "mark $readlimit")
        //Do nothing - mark/reset is not supported
    }

    override fun reset() {
        Log.d(LOGTAG, "reset")
        throw java.io.IOException("mark/reset not supported")
    }

    override fun available(): Int {
        return if(contentLength >= 0) {
            (contentLength - bytesReadOrSkipped).coerceIn(0L, Int.MAX_VALUE.toLong()).toInt().also {
                Log.d(LOGTAG, "available: $it bytes")
            }
        }else {
            super.available().also {
                Log.d(LOGTAG, "super.available: $it bytes")
            }
        }
    }

    override fun close() {
        Log.d(LOGTAG, "close")
        super.close()
    }

    companion object {

        const val LOGTAG = "ContentLengthInputStreamWrapper"

    }
}
