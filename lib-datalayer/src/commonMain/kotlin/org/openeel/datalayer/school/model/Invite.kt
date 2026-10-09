package world.respect.datalayer.school.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import world.respect.datalayer.school.model.Invite2.Companion.TYPE_CLASS
import world.respect.datalayer.school.model.Invite2.Companion.TYPE_FAMILY_MEMBER
import world.respect.datalayer.school.model.Invite2.Companion.TYPE_NEW_USER
import world.respect.datalayer.shared.ModelWithTimes
import world.respect.lib.serializers.InstantAsISO8601
import world.respect.libutil.ext.CHAR_POOL_NUMBERS
import world.respect.libutil.ext.randomString
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * An invitation for RESPECT can be one of three types.
 *
 * Serialization is as per:
 * https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md#custom-subclass-serial-name
 *
 * @property uid the uid will be in the form of invite type:details e.g. newuser:role, class:role/classuid etc.
 *           this ensures there is one invite per type.
 * @property code the invite code
 * @property approvalRequiredAfter Users with permission to invite others may turn off the
 *           requirement to approve a user for a short interval (e.g. 15 minutes). This allows
 *           skipping the admin approval requirement (e.g. during teacher training workshops)
 *           whilst mitigating the risk that the link might be shared with unauthorized people later.
 */
@Serializable
sealed interface Invite2: ModelWithTimes {
    val uid: String
    val code: String
    val approvalRequiredAfter: InstantAsISO8601
    override val lastModified: InstantAsISO8601
    override val stored: InstantAsISO8601

    val status: StatusEnum


    companion object {

        const val TYPE_NEW_USER = "newuser"

        const val TYPE_CLASS = "class"

        const val TYPE_FAMILY_MEMBER = "familymember"

        const val TABLE_ID = 17

        const val APPROVAL_NOT_REQUIRED_INTERVAL_MINS = 15

        const val DEFAULT_CODE_LEN = 10

        const val VIA_PARENT_STR = "via_parent"

        const val DIRECT_STR = "direct"

        fun newRandomCode(
            codeLen: Int = DEFAULT_CODE_LEN,
            charPool: String = CHAR_POOL_NUMBERS
        ): String = randomString(codeLen, charPool)


    }
}

/**
 * @property firstUser - special property that will be set if the invite was created as part of
 *           self-service add your school. This invite will not require approval (there would be no
 *           one to approve it). After it is used, it will be deleted.
 */
@Serializable
@SerialName(TYPE_NEW_USER)
data class NewUserInvite(
    override val uid: String,
    override val code: String,
    override val approvalRequiredAfter: InstantAsISO8601 = Instant.fromEpochMilliseconds(0),
    override val lastModified: InstantAsISO8601 = Clock.System.now(),
    override val stored: InstantAsISO8601 = Clock.System.now(),
    override val status: StatusEnum = StatusEnum.ACTIVE,
    val role: PersonRoleEnum,
    val firstUser: Boolean = false,
): Invite2

/**
 *
 * @property inviteMode if set to VIA_PARENT, then the role when accepting this invite will be that
 *           of parent. The parent will first enter their own information, then their child's
 *           information.
 */
@Serializable
@SerialName(TYPE_CLASS)
data class ClassInvite(
    override val uid: String,
    override val code: String,
    override val approvalRequiredAfter: InstantAsISO8601 = Instant.fromEpochMilliseconds(0),
    override val lastModified: InstantAsISO8601 = Clock.System.now(),
    override val stored: InstantAsISO8601 = Clock.System.now(),
    override val status: StatusEnum = StatusEnum.ACTIVE,
    val classUid: String,
    val role: EnrollmentRoleEnum,
    val inviteMode: ClassInviteModeEnum = ClassInviteModeEnum.DIRECT,
): Invite2 {

    companion object {

        fun uidFor(
            classUid: String,
            role: EnrollmentRoleEnum,
            inviteMode: ClassInviteModeEnum,
        ): String {
            return "$TYPE_CLASS:${role.value}/${inviteMode.value}/$classUid"
        }

    }
}

@Serializable
@SerialName(TYPE_FAMILY_MEMBER)
data class FamilyMemberInvite(
    override val uid: String,
    override val code: String,
    override val approvalRequiredAfter: InstantAsISO8601,
    override val lastModified: InstantAsISO8601 = Clock.System.now(),
    override val stored: InstantAsISO8601 = Clock.System.now(),
    override val status: StatusEnum = StatusEnum.ACTIVE,
    val personUid: String,
): Invite2

