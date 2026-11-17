package com.ustadmobile.libcache.webview

import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.ustadmobile.libcache.okhttp.headersContentLength
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.charset
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.http.promisesBody
import java.io.ByteArrayInputStream

/**
 * A WebViewClient that uses OKHttp to make requests by overriding the shouldInterceptRequest
 * function (thus working with anything that works with OKHttp e.g. UstadCache).
 */
class OkHttpWebViewClient(
    private val shouldInterceptRequestFilter: ShouldInterceptRequestFilter = DefaultShouldInterceptRequestFilter(),
    private val requestCaller: RequestCaller,
) : WebViewClient() {

    fun interface RequestCaller {

        operator fun invoke(request: Request): Call

    }

    //Could be replaced with call factory
    class OkHttpClientRequestCaller(
        private val client: OkHttpClient,
    ): RequestCaller {
        override fun invoke(request: Request): Call {
            return client.newCall(request)
        }
    }

    fun interface ShouldInterceptRequestFilter {
        fun shouldIntercept(request: WebResourceRequest): Boolean
    }

    constructor(
        shouldInterceptRequestFilter: ShouldInterceptRequestFilter = DefaultShouldInterceptRequestFilter(),
        okHttpClient: OkHttpClient,
    ): this(
        shouldInterceptRequestFilter = shouldInterceptRequestFilter,
        requestCaller = OkHttpClientRequestCaller(okHttpClient),
    )

    class DefaultShouldInterceptRequestFilter: ShouldInterceptRequestFilter {
        override fun shouldIntercept(request: WebResourceRequest): Boolean {
            /* WebResourceRequest has no way to get any request body, so it cannot be used to handle
             * PUT, POST, etc.
             */
            if(request.method.uppercase().let { it != "GET" && it != "HEAD" })
                return false

            return request.url.host.let { it !in DEFAULT_DONT_INTERCEPT_HOSTS }
        }
    }

    /**
     * Handle intercepting the request and passing to OKHttp if required.
     *
     * @return if shouldInterceptRequestFilter returns true for the request, then return a response
     *         adapted from the OKHttpResponse. Otherwise return null
     */
    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        if(request != null && shouldInterceptRequestFilter.shouldIntercept(request)) {
            return try {
                val response = requestCaller(
                    Request.Builder()
                        .url(request.url.toString())
                        .apply {
                            method(request.method, null)
                            request.requestHeaders.entries.forEach {
                                header(it.key, it.value)
                            }
                        }
                        .build()
                ).execute()

                val httpStatusCode = HttpStatusCode.fromValue(response.code)
                val contentLength = response.headersContentLength()

                val contentType = ContentType.parse(
                    response.header("content-type") ?: "application/octet-stream"
                )
                val mimeType = "${contentType.contentType}/${contentType.contentSubtype}"

                Log.d("UstadCacheWebViewClient", "Intercept ${request.url} " +
                        "(${response.code} ${httpStatusCode.description}) " +
                        "$mimeType (charset=${contentType.charset()}) $contentLength bytes"
                )

                val responseBody = response.takeIf { it.promisesBody() }?.body

                // See https://issues.chromium.org/issues/40739128#comment24
                /*
                 * As per:
                 * https://developer.android.com/reference/android/webkit/WebResourceResponse#WebResourceResponse(java.lang.String,%20java.lang.String,%20java.io.InputStream)
                 * The mimeType must be the mimetype ONLY (without the charset, parameters, etc)
                 * The encoding is the charset (if provided), otherwise null (as expected for non-text
                 * responses such as images).
                 */
                return WebResourceResponseWorkaround(
                    mimeType,
                    contentType.charset()?.name(),
                    response.code,
                    HttpStatusCode.fromValue(response.code).description,
                    response.headers.toMap(),
                    responseBody?.byteStream() ?: ByteArrayInputStream(byteArrayOf()),
                )
            }catch(e: Throwable) {
                Log.w("UstadWebViewClient", "Error intercepting request", e)
                val responseBytes = "OkHttpWebViewClient error: ${e.message}".toByteArray()

                WebResourceResponse(
                    "text/plain",
                    "UTF-8",
                    503,
                    "Service Unavailable",
                    buildMap {
                        put("Content-Type", "text/plain")
                        put("Content-Length", responseBytes.size.toString())
                        put("Cache-Control", "no-cache, no-store, must-revalidate")
                    },
                    ByteArrayInputStream(responseBytes)
                )
            }
        }else {
            return null
        }
    }


    /**
     * Returns false to prevent links from opening in browser or other app
     */
    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(
        view: WebView?,
        url: String?
    ): Boolean {
        return false
    }

    /**
     * Returns false to prevent links from opening in browser or other app
     */
    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        return false
    }

    companion object {

        val DEFAULT_DONT_INTERCEPT_HOSTS = listOf("localhost", "127.0.0.1")

        val RESERVED_HEADERS = listOf("Content-Type", "Content-Length", "Content-Encoding")

        private val WEBVIEW_HANDLED_REQUEST_HEADERS = setOf("range", "if-range")

        private val WEBVIEW_GENERATED_RESPONSE_HEADERS = setOf(
            "content-type", "content-length"
        )


    }
}