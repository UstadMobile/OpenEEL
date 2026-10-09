package world.respect.lib.dataloadstate

import io.ktor.http.Headers
import io.ktor.http.Url
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Combined metadata (e.g. data about data) on loaded data. This includes the loading status and
 * validation info (e.g. last modified time and etags as available if this dataload was from an
 * http request).
 *
 * @param lastModified when the data was last modified. When data is fetched over http, this can be
 *         - determined by subtracting the age header (delta seconds) from the system clock.
 *         - parsed from the last-modified header.
 *
 *         When loading and storing local data, the last modified time is tracked as a field in the
 *         database.
 *
 *         When this is unknown/not applicable - e.g. whilst still awaiting a response, then it will
 *         be -1L
 *
 * @param etag etag as provided by the origin server (if any)
 *
 * @param url the URL
 *
 * @param consistentThrough where this is from a remote server that provides an X-Consistent-Through
 *        Header ( as per datalayer README.md), this is the timestamp in millis since epoch.
 *
 * @param validationInfoKey validation info key as per ExtendedDataSourceValidationHelper.validationInfoKey
 *
 * @param varyHeader where this is from a remote server that provides an HTTP vary header, this is
 *        the value of the vary header (such that a varyHash can be calculated for subsequent requests).
 *
 * @param permissionsLastModified often required for subsequent requests that use the
 *        consistentThrough header to set GetListCommonParams.since
 */
@Serializable
data class DataLoadMetaInfo(

    val lastModified: Long = -1,

    val lastStored: Long = -1,

    val etag: String? = null,

    val url: Url? = null,

    val consistentThrough: Instant? = null,

    val validationInfoKey: Long = 0,

    val varyHeader: String? = null,

    val headers: Headers = Headers.Empty,

    val permissionsLastModified: Instant? = null,

) {

    fun requireUrl() = url ?: throw IllegalStateException("requireUrl: load meta info has no url")

}


