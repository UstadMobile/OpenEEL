package org.openeel.datalayer.db.networkvalidation

import com.ustadmobile.ihttp.headers.IHttpHeaders
import io.ktor.http.Url
import org.openeel.lib.dataloadstate.DataLoadMetaInfo
import org.openeel.datalayer.db.RespectAppDatabase
import org.openeel.datalayer.db.networkvalidation.entities.NetworkValidationInfoEntity
import org.openeel.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import org.openeel.datalayer.networkvalidation.NetworkValidationInfo
import org.openeel.libutil.util.time.systemTimeInMillis
import org.openeel.libxxhash.XXHasher64Factory
import org.openeel.libxxhash.XXStringHasher

class ExtendedDataSourceValidationHelperImpl(
    private val respectAppDb: RespectAppDatabase,
    private val xxStringHasher: XXStringHasher,
    private val xxHasher64Factory: XXHasher64Factory,
) : ExtendedDataSourceValidationHelper {

    override suspend fun getValidationInfo(
        url: Url,
        requestHeaders: IHttpHeaders
    ): NetworkValidationInfo? {

        //first lookup the most recent vary header for the url
        val urlHash = xxStringHasher.hash(url.toString())
        val lastVaryHeader = respectAppDb.getNetworkValidationInfoEntityDao()
            .getLatestVaryHeaderByUrlHash(urlHash)

        if(lastVaryHeader?.trim() == "*")
            return null

        val validationInfoKey = validationInfoKey(requestHeaders, lastVaryHeader)

        return respectAppDb.getNetworkValidationInfoEntityDao()
            .getValidationInfo(urlHash, validationInfoKey)?.let {
                NetworkValidationInfo(
                    etag = it.nviEtag,
                    lastModified = it.nviLastModified,
                    consistentThrough = it.nviConsistentThrough,
                    varyHeader = it.nviVaryHeader,
                    validationInfoKey = validationInfoKey,
                    lastChecked = it.nviLastChecked,
                )
            }
    }

    override fun validationInfoKey(
        requestHeaders: IHttpHeaders,
        varyHeader: String?
    ): Long {
        val hasher = xxHasher64Factory.newHasher(0)
        if(varyHeader == null)
            return 0

        val responseHeaderTrimmed = varyHeader.trim()
        if(responseHeaderTrimmed == "*") {
            return -1L //not cacheable, cannot be hashed
        }

        val varyHeaderNames = responseHeaderTrimmed.split(",").map {
            it.trim().lowercase()
        }.sorted()

        varyHeaderNames.forEach { headerName ->
            hasher.update(headerName.toByteArray())
            requestHeaders.getAllByName(headerName).sorted().forEach { headerVal ->
                val headerValBytes = headerVal.toByteArray()
                hasher.update(headerValBytes)
            }
        }

        return hasher.digest()
    }

    override suspend fun updateValidationInfo(
        metaInfo: DataLoadMetaInfo
    ) {
        respectAppDb.getNetworkValidationInfoEntityDao().upsert(
            NetworkValidationInfoEntity(
                nviUrlHash = xxStringHasher.hash(metaInfo.requireUrl().toString()),
                nviKey = metaInfo.validationInfoKey,
                nviVaryHeader = metaInfo.varyHeader,
                nviEtag = metaInfo.etag,
                nviLastModified = metaInfo.lastModified,
                nviConsistentThrough = metaInfo.consistentThrough,
                nviLastChecked = systemTimeInMillis(),
            )
        )
    }



}