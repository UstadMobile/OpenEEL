package org.openeel.datalayer.http

import io.ktor.client.HttpClient
import org.openeel.datalayer.SchoolDirectoryDataSource
import org.openeel.datalayer.SchoolDirectoryDataSourceLocal
import org.openeel.datalayer.http.schooldirectory.SchoolDirectoryEntryResourceHttpClient
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryResource
import org.openeel.datalayer.schooldirectory.SchoolDirectoryResource

class SchoolDirectoryDataSourceHttp(
    private val httpClient: HttpClient,
    private val local : SchoolDirectoryDataSourceLocal,
): SchoolDirectoryDataSource {

    override val schoolDirectoryResource: SchoolDirectoryResource
        get() = throw IllegalArgumentException("There is no http data source for directory list")

    override val schoolDirectoryEntryResource: SchoolDirectoryEntryResource by lazy {
        SchoolDirectoryEntryResourceHttpClient(
            httpClient = httpClient,
            local = local,
        )
    }
}
