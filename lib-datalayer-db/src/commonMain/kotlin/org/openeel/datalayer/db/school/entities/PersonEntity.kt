package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject
import world.respect.datalayer.school.model.PersonGenderEnum
import world.respect.datalayer.school.model.PersonStatusEnum

/**
 * @property pGuid the uid of the person
 */
@Entity
data class PersonEntity(
    val pGuid: String,
    @PrimaryKey
    val pGuidHash: Long,
    val pActive: Boolean,
    val pStatus: PersonStatusEnum,
    val pLastModified: Long,
    val pStored: Long,
    val pMetadata: JsonObject?,
    val pUsername: String? = null,
    val pGivenName: String,
    val pFamilyName: String,
    val pMiddleName: String? = null,
    val pGender: PersonGenderEnum,
    val pDateOfBirth: LocalDate? = null,
    val pEmail: String? = null,
    val pPhoneNumber: String? = null
)