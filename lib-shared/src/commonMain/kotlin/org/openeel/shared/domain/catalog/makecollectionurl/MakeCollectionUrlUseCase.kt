package org.openeel.shared.domain.catalog.makecollectionurl

import io.ktor.http.URLBuilder
import io.ktor.http.Url
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryResource
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.xapi.OpenEelXapiConstants
import org.openeel.libutil.ext.appendEndpointPathSegments
import org.openeel.libutil.ext.appendEndpointSegments
import kotlin.uuid.Uuid

class MakeCollectionUrlUseCase(
    private val schoolUrl: Url,
    private val schoolDirectoryEntryResource: SchoolDirectoryEntryResource,
) {

    suspend operator fun invoke() : Url {
        val xapiUrl = schoolDirectoryEntryResource.getSchoolDirectoryEntryByUrl(
            schoolUrl
        ).dataOrNull()?.xapi ?: throw IllegalStateException("No Xapi URL for $schoolUrl")

        return URLBuilder(xapiUrl).apply {
            appendEndpointPathSegments(listOf("activities", "profile"))
            encodedParameters["activityId"] = UrlEncoderUtil.encode(
                schoolUrl.appendEndpointSegments("collections", Uuid.random().toString()).toString()
            )
            encodedParameters["profileId"] = UrlEncoderUtil.encode(
                OpenEelXapiConstants.ACTIVITY_PROFILEID_OPDS_COLLECTION
            )
        }.build()
    }

}