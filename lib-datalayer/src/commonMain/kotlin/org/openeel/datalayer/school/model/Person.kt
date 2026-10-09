package world.respect.datalayer.school.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import world.respect.datalayer.shared.ModelWithTimes
import world.respect.lib.serializers.InstantAsISO8601
import kotlin.time.Clock

/**
 * @property guid unique system identifier. As per various specs it MUST NOT contain personal
 *           information (e.g. username, name, etc). It could be a sequential key, etc.
 * @property userMasterIdentifier generally an ID assigned by the organization (e.g. government
 *           issued student/teacher id number)
 * @property userActive (should be changed to status enum - which may be pending)
 * @property status PersonStatusEnum - can be pending approval.
 * @property relatedPersonUids  Used to link a student to their parent/guardian.
 */

@Serializable
data class Person(
    val guid: String,
    val userActive: Boolean = true,
    val status: PersonStatusEnum = PersonStatusEnum.ACTIVE,
    override val lastModified: InstantAsISO8601 = Clock.System.now(),
    override val stored: InstantAsISO8601 = Clock.System.now(),
    val metadata: JsonObject? = null,
    val userMasterIdentifier: String? = null,
    val username: String? = null,
    val givenName: String,
    val familyName: String,
    val middleName: String? = null,
    val gender: PersonGenderEnum,
    val preferredFirstName: String? = null,
    val preferredMiddleName: String? = null,
    val preferredLastName: String? = null,
    val pronouns: String? = null,
    val roles: List<PersonRole>,
    val relatedPersonUids: List<String> = emptyList(),
    val dateOfBirth: LocalDate? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
) : ModelWithTimes {

    companion object {
        const val TABLE_ID = 2

        const val METADATA_KEY_INVITE_ID = "inviteCode"

        const val METADATA_KEY_INVITE_UID = "inviteUid"

        const val DEVICE_INFO = "deviceInfo"

        const val DEVICE_MODEL = "deviceModel"

        const val DEVICE_PLATFORM = "devicePlatform"

        const val DEVICE_OS_VERSION = "deviceOsVersion"

    }

}