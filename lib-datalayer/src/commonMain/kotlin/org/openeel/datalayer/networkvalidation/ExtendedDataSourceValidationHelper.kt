package world.respect.datalayer.networkvalidation

import com.ustadmobile.ihttp.headers.IHttpHeaders
import world.respect.lib.dataloadstate.DataLoadMetaInfo

interface ExtendedDataSourceValidationHelper: BaseDataSourceValidationHelper {

    /**
     * The validationInfoKey enables the validation helper to distinguish between different requests
     * for the same URL, which is mostly based on the the http vary header (e.g. where a response
     * is specified to vary based on the authorization used to make the request, arguments in
     * headers, etc).
     *
     * It excludes the since parameter (similar to how if-modified-since and if-none-match are not
     * included in the vary header even though their values lead to different responses).
     *
     * @param requestHeaders the HTTP request headers for the request to be made
     * @param varyHeader the value of the vary header from the last response
     *
     * @return a hash of the vary headers value to use as a key
     */
    fun validationInfoKey(
        requestHeaders: IHttpHeaders,
        varyHeader: String?
    ): Long

    /**
     * Invoked by the repository when a response is received and processed
     */
    suspend fun updateValidationInfo(
        metaInfo: DataLoadMetaInfo,
    )

}