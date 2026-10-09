package world.respect.datalayer.shared

/**
 * Interface that is implemented on RESPECT Repository DataSources that includes a local model
 * DataSource and a writable remote datasource. This allows common logic in the remote write queue
 * to work for different data model types.
 */
interface RepositoryModelDataSource<T: Any> {

    val local: LocalModelDataSource<T>

    val remote: WritableDataSource<T>

}