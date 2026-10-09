package world.respect.datalayer.db.shared.entities

/**
 * Common interface for LangMapEntity classes. Creating an additional LangMapEntity class per
 * related entity (e.g. SchoolDirectoryEntry, RespectAppManifestEntry, etc) makes it easier to use
 * Room's Relation annotation.
 */
interface ILangMapEntity {

    val lang: String

    val region: String?

    val value: String

}
