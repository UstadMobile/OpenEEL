package world.respect.datalayer.networkvalidation

import com.ustadmobile.ihttp.headers.IHttpHeaders
import io.ktor.http.Url

/**
 * A DataSource validation helper is used by HTTP based datasources to store and retrieve validation
 * info (such as last-modified, etag, consistent-through). The validation info is required by the
 * http based data source when it is about to make a request so that it can a) include
 * if-modified-since and if-none-match headers and b) decide if sending a request is even required
 * (e.g. if cache-control indicates the previous response is still valid). Using validation headers
 * prevents unnecessary data transfer and avoids unnecessary database updates (which also helps
 * avoid unnecessary UI updates).
 *
 * There are two potential implementations:
 *
 * "Base": When there is a 1:1 relationship between the URL loaded and the entity in the database,
 * it is generally most efficient to simply store the validation info as part of the db entity itself.
 * Only standard cache validation headers (etag, last-modified, cache-control) are used. This is
 * generally the case for Respect Compatible app manifests, OPDS catalogs, etc.
 *
 * "Extended": Sometimes there isn't a 1:1 relationship between HTTP requests and entities stored in
 * the database e.g. when http requests use a REST API that returns a list of entities (e.g.
 * Xapi Statements, Persons, etc). When the REST API provides a consistent-through response header
 * and since request parameter (which filters the response based on the entities stored time) this
 * makes it possible to retrieve only the entities that were changed since the last check.
 */
interface BaseDataSourceValidationHelper {

    /**
     * Get validation info to be used for an outgoing request to set the if-modified-since header
     * and if-none-match header.
     */
    suspend fun getValidationInfo(
        url: Url,
        requestHeaders: IHttpHeaders,
    ): NetworkValidationInfo?

}