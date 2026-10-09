package world.respect.datalayer.db.schooldirectory.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.ktor.http.Url
import world.respect.datalayer.respect.model.invite.AuthOptionConfigTypeEnum

/**
 * Entity that represents AuthenticationOption model, used via a 1:many join from [SchoolDirectoryEntity]
 *
 * @property sdeAoUid uid for the entity itself
 * @property sdeAoRdUid the foreign key for [SchoolDirectoryEntity.rdUid]
 *
 */
@Entity
class SchoolDirectoryEntryAuthOptionEntity(
    @PrimaryKey(autoGenerate = true)
    val sdeAoUid: Long = 0,
    val sdeAoRdUid: Long = 0,
    val sdeAoName: String,
    val sdeAoConfigType: AuthOptionConfigTypeEnum,
    val sdeAoOpenIdIssuerUrl: Url?,
)
