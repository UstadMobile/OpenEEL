package world.respect.datalayer.db.school.opds

import world.respect.datalayer.db.shared.entities.LangMapEntity
/**
 * The OPDS network models are represented in the database using various 1:many joins e.g. with
 * ReadiumLinkEntity, ReadiumSubjectEntity etc. These joins often represent nested
 * relationships e.g. A ReadiumLinkEntity may represent a ReadiumLink which is a child of
 * another ReadiumLink (via the children, subcollection, or alternate properties).
 *
 * All OPDS entities have a parent that is an OpdsFeed or OpdsPublication. All entities
 * keep a direct reference to the top level parent so that:
 * a) Select queries can easily select all entities related to a given OpdsFeed or OpdsPublication
 *    which the adapter can then reassemble into the network model
 * b) Upsert queries (used when data is updated from the network) can easily delete entities related
 *    to old versions of an OpdsFeed or OpdsPublication
 *
 * When an entity is within a publication that is also part of an OPDS Feed, then the publication's
 * uid is considered the top level parent uid
 */
enum class OpdsParentType(
    val id: Int,
    val langMapTopParentType: LangMapEntity.TopParentType,
) {

    OPDS_FEED(1, LangMapEntity.TopParentType.OPDS_FEED),
    OPDS_PUBLICATION(2, LangMapEntity.TopParentType.OPDS_PUBLICATION);

    companion object {

        const val ID_FEED = 1

        const val ID_PUBLICATION = 2

    }

}
