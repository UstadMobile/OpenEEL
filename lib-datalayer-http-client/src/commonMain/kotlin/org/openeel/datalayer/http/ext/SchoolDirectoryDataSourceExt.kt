package org.openeel.datalayer.http.ext

import io.ktor.http.Url
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.datalayer.respect.model.SchoolDirectoryEntry
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import org.openeel.libutil.ext.appendEndpointSegments

suspend fun SchoolDirectoryEntryDataSource.schoolDirectoryEntryOrNull(
    schoolUrl: Url
): SchoolDirectoryEntry? {
    val schoolDirectoryData = getSchoolDirectoryEntryByUrl(schoolUrl)
    return schoolDirectoryData.dataOrNull()
}

suspend fun SchoolDirectoryEntryDataSource.resolveRespectExtUrlForSchool(
    schoolUrl: Url,
    resourcePath: String,
): Url {
    val schoolDirectory = schoolDirectoryEntryOrNull(schoolUrl)

    return schoolDirectory?.respectExt?.appendEndpointSegments(resourcePath)
        ?: throw IllegalStateException("SchoolUrl $schoolUrl has no respect extensions URL")
}

suspend fun SchoolDirectoryEntryDataSource.resolveXapiUrlForSchool(
    schoolUrl: Url,
    resourcePath: String,
): Url {
    val schoolDirectory = schoolDirectoryEntryOrNull(schoolUrl)

    return schoolDirectory?.xapi?.appendEndpointSegments(resourcePath)
        ?: throw IllegalStateException("SchoolUrl $schoolUrl has no respect extensions URL")
}

