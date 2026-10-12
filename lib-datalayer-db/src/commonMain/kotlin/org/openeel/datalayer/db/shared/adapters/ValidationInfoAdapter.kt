package org.openeel.datalayer.db.shared.adapters

import org.openeel.datalayer.db.shared.LastModifiedAndETagDb
import org.openeel.datalayer.networkvalidation.NetworkValidationInfo

fun LastModifiedAndETagDb.asNetworkValidationInfo(): NetworkValidationInfo {
    return NetworkValidationInfo(
        lastModified = lastModified,
        etag = etag,
    )
}