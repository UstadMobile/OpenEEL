package world.respect.datalayer.db.shared

data class LastModifiedAndETagDb(
    val lastModified: Long,
    val etag: String?,
)