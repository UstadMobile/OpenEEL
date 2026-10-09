package world.respect.datalayer.db.schooldirectory.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import world.respect.datalayer.db.shared.entities.ILangMapEntity

@Entity
data class SchoolDirectoryEntryLangMapEntity(
    @PrimaryKey(autoGenerate = true)
    val sdelUid: Int = 0,
    val sdelReUid: Long,
    val sdelLang: String,
    val sdelRegion: String?,
    val sdelValue: String,
    val sdelPropId: Int = 0,
): ILangMapEntity {

    override val lang: String
        get() = sdelLang
    override val region: String?
        get() = sdelRegion
    override val value: String
        get() = sdelValue
}
