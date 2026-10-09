package world.respect.datalayer.db.shared.adapters

import world.respect.datalayer.db.shared.LastModifiedAndETagDb
import world.respect.datalayer.networkvalidation.NetworkValidationInfo

fun LastModifiedAndETagDb.asNetworkValidationInfo(): NetworkValidationInfo {
    return NetworkValidationInfo(
        lastModified = lastModified,
        etag = etag,
    )
}