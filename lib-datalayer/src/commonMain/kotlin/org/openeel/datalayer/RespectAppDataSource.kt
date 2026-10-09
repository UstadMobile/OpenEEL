package world.respect.datalayer

import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSource
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource

/**
 * DataSource that provides app (eg. system) level data that is NOT specific to a given school
 * e.g. federated (see ARCHITECTURE.md for more info).
 *
 * This includes:
 * a) RESPECT Compatible app manifests (loaded over http, cached locally, loaded offline-first)
 * b) OPDS feeds (loaded over http, cached locally, loaded offline-first)
 * c) Directory of available schools:
 *  i) On the server this is the list of schools run on that given server
 *  ii) On the client this is the list of known directory servers and schools as listed by those servers
 *
 */
interface RespectAppDataSource {

    val schoolDirectoryDataSource: SchoolDirectoryDataSource

    val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource

}
