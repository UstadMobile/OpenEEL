package world.respect.datalayer.db.shared.adapters

import world.respect.datalayer.db.shared.entities.ILangMapEntity

/**
 * Functional interface that enables sharing logic between ILangMapEntity classes for different
 * tables. See ILangMapEntity for notes
 */
fun interface ILangMapEntityAdapter<T: ILangMapEntity> {

    operator fun invoke(
        language: String,
        region: String?,
        value: String,
    ): T

}