package world.respect.datalayer.shared.paging

import androidx.paging.PagingSource
import world.respect.lib.dataloadstate.DataLoadMetaInfo
import world.respect.lib.dataloadstate.throwable.ExceptionWithHttpStatusCode

/**
 * A Cacheable Http Paging Source is one that loads data over http that can be cached by a
 * local datasource (e.g. an implementation of LocalModelDataSource).
 *
 * As per BaseDataSourceValidationHelper there are two potential implementations of data layer
 * caching: where there is a 1:1 relationship between the URL loaded and entities stored in the
 * database, and an extended one where this is not the case.
 *
 * In the extended case the validation ( ExtendedDataSourceValidationHelper ), the response
 * validation info is separate and must separately updated after the data from the underlying
 * request data has been successfully stored.
 *
 * The Repository Paging Source is responsible to call onLoadResultStored after successfully
 * storing data. Subsequent http requests can then use the validation data stored to add a
 * Last-Modified and Etag information to outgoing requests.
 */
interface CacheableHttpPagingSource<Key: Any, Value: Any> {

    /**
     * When the load function is called and we get a 304 NotModified, we need to return this somehow.
     * Its not really an exception, but there's no cleaner way to represent it.
     *
     * This will only happen when using our own validation helper, which is put there to work with
     * the repository.
     */
    class NotModifiedNonException(
        val metaInfo: DataLoadMetaInfo
    ): Exception(), ExceptionWithHttpStatusCode {
        override val statusCode: Int
            get() = 304
    }

    /**
     * To be called by a repository after successfully storing a cacheable loadResult. This will
     * lead to subsequent requests using if-modified-since / if-none-match headers on outgoing
     * requests.
     */
    suspend fun onLoadResultStored(
        loadResult: PagingSource.LoadResult<Key, Value>
    )

}