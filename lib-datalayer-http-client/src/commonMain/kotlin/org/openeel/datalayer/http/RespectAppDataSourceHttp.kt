package world.respect.datalayer.http

import io.ktor.client.HttpClient
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.RespectAppDataSourceLocal
import world.respect.datalayer.http.schooldirectory.SchoolDirectoryEntryDataSourceHttpClient
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSource
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource

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
