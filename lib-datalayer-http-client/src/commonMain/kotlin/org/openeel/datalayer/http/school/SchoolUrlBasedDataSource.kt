package world.respect.datalayer.http.school

import io.ktor.http.Url
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource

/**
 * Interface for http SchoolDataSources. This is used by various school-scope HTTP datasources.
 *
 * Getting the URL for a particular endpoint (e.g. Respect extensions, xAPI, etc) is itself
 * asynchronous because this requires loading the SchoolDirectoryEntry from the database and/or
 * network to get the base URL
 *
 *
 */
interface SchoolUrlBasedDataSource {

    /**
     * School base URL as per SchoolDirectoryEntry.self
     */
    val schoolUrl: Url

    /**
     * School Directory DataSource that can be used to load the SchoolDirectoryEntry for this school
     */
    val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource

}