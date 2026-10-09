package world.respect.datalayer.http.ext

import io.ktor.http.Url
import world.respect.datalayer.http.school.SchoolUrlBasedDataSource

suspend fun SchoolUrlBasedDataSource.respectEndpointUrl(resourcePath: String): Url {
    return schoolDirectoryEntryDataSource.resolveRespectExtUrlForSchool(schoolUrl, resourcePath)
}

suspend fun SchoolUrlBasedDataSource.xapiEndpointUrl(resourcePath: String): Url {
    return schoolDirectoryEntryDataSource.resolveXapiUrlForSchool(schoolUrl, resourcePath)
}
