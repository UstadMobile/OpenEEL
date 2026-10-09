package world.respect.datalayer.db.school.opds.ext

import world.respect.datalayer.db.school.opds.entities.OpdsFeedEntity
import world.respect.lib.dataloadstate.ETagAndLastModified

fun OpdsFeedEntity.etagAndLastModified(): ETagAndLastModified {
    return ETagAndLastModified(
        etag = this.ofeEtag,
        lastModified = this.ofeLastModifiedHeader,
    )
}
