package world.respect.datalayer.db.school.opds.adapters

import world.respect.datalayer.db.school.opds.entities.OpdsFeedMetadataEntity
import world.respect.lib.opds.model.OpdsFeedMetadata

fun OpdsFeedMetadata.asEntity(
    ofmeOfeUid: Long,
    ofmePropType: OpdsFeedMetadataEntity.PropType,
    ofmeRelUid: Long,
): OpdsFeedMetadataEntity {
    return OpdsFeedMetadataEntity(
        ofmeOfeUid = ofmeOfeUid,
        ofmePropType = ofmePropType,
        ofmePropFk = ofmeRelUid,
        ofmeIdentifier = identifier,
        ofmeType = type,
        ofmeTitle = title,
        ofmeSubtitle = subtitle,
        ofmeModified = modified,
        ofmeDescription = description,
        ofmeItemsPerPage = itemsPerPage,
        ofmeCurrentPage = currentPage,
        ofmeNumberOfItems = numberOfItems,
    )
}

fun OpdsFeedMetadataEntity.asModel(): OpdsFeedMetadata {
    return OpdsFeedMetadata(
        identifier = ofmeIdentifier,
        type = ofmeType,
        title = ofmeTitle,
        subtitle = ofmeSubtitle,
        modified = ofmeModified,
        description = ofmeDescription,
        itemsPerPage = ofmeItemsPerPage,
        currentPage = ofmeCurrentPage,
        numberOfItems = ofmeNumberOfItems,
    )
}
