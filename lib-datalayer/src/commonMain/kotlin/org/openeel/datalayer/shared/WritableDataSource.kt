package world.respect.datalayer.shared

/**
 * Interface representing a writable datasource.
 */
interface WritableDataSource<T: Any> {

    /**
     * The store function is used to store new or modified data and implemented on DataSources for
     * models (e.g. Class, Person, Enrollment etc) that can be written. These data sources are (as
     * documented on SchoolDataSource) tied a specific authenticated user for in a specific school.
     *
     * It is generally implemented as follows:
     * - Database: checks permissions, if permissions are OK, then saves data to the
     *   database. If permission check fails, throws ForbiddenException
     * - HTTP/Remote: Sends a post request to the remote server. The remote server will
     *   probably then call its database's store function. When permission checks fail, this results
     *   in an HTTP 403 error.
     * - Repository: updates the local database, and then queues the data to the RemoteWriteQueue
     *   to be sent to the remote datasource.
     */
    suspend fun store(list: List<T>)

}