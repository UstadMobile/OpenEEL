package org.openeel.datalayer.db.school.opds.ext

import org.openeel.datalayer.db.school.opds.entities.OpdsFeedEntity
import org.openeel.lib.dataloadstate.ETagAndLastModified

fun OpdsFeedEntity.etagAndLastModified(): ETagAndLastModified {
    return ETagAndLastModified(
        etag = this.ofeEtag,
        lastModified = this.ofeLastModifiedHeader,
    )
}
