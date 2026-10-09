package world.respect.server.util.ext

import com.ustadmobile.ihttp.ext.clientProtocolAndHost
import com.ustadmobile.ihttp.headers.asIHttpHeaders
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.fromHttpToGmtDate
import io.ktor.server.request.ApplicationRequest
import kotlin.time.Instant

val ApplicationRequest.virtualHost: Url
    get() = Url(headers.asIHttpHeaders().clientProtocolAndHost())
