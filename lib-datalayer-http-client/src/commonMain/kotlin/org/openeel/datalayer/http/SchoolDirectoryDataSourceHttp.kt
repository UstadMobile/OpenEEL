package org.openeel.datalayer.http

import io.ktor.client.HttpClient
import org.openeel.datalayer.SchoolDirectoryDataSource
import org.openeel.datalayer.SchoolDirectoryDataSourceLocal
import org.openeel.datalayer.http.schooldirectory.SchoolDirectoryEntryDataSourceHttpClient
import org.openeel.datalayer.schooldirectory.SchoolDirectoryDataSource
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryDataSource

class SchoolDirectoryDataSourceHttp(
    private val httpClient: HttpClient,
    private val local : SchoolDirectoryDataSourceLocal,
): SchoolDirectoryDataSource {

    override val schoolDirectoryDataSource: org.openeel.datalayer.schooldirectory.SchoolDirectoryDataSource
        get() = throw IllegalArgumentException("There is no http data source for directory list")

    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource by lazy {
        SchoolDirectoryEntryDataSourceHttpClient(
            httpClient = httpClient,
            local = local,
        )
    }
}
