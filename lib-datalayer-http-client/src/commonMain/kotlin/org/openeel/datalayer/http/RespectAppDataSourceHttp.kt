package org.openeel.datalayer.http

import io.ktor.client.HttpClient
import org.openeel.datalayer.RespectAppDataSource
import org.openeel.datalayer.RespectAppDataSourceLocal
import org.openeel.datalayer.http.schooldirectory.SchoolDirectoryEntryDataSourceHttpClient
import org.openeel.datalayer.schooldirectory.SchoolDirectoryDataSource
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryDataSource

class RespectAppDataSourceHttp(
    private val httpClient: HttpClient,
    private val local : RespectAppDataSourceLocal,
): RespectAppDataSource {

    override val schoolDirectoryDataSource: SchoolDirectoryDataSource
        get() = throw IllegalArgumentException("There is no http data source for directory list")

    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource by lazy {
        SchoolDirectoryEntryDataSourceHttpClient(
            httpClient = httpClient,
            local = local,
        )
    }
}
