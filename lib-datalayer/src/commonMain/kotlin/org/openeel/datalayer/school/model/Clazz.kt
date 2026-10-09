package world.respect.datalayer.school.model

import kotlinx.serialization.Serializable
import world.respect.datalayer.shared.ModelWithTimes
import world.respect.lib.serializers.InstantAsISO8601
import kotlin.random.Random
import kotlin.time.Clock

@Serializable
data class Clazz(
    val guid: String,
    val title: String,
    val status: StatusEnum = StatusEnum.ACTIVE,
    val description: String? = null,
    override val lastModified: InstantAsISO8601 = Clock.System.now(),
    override val stored: InstantAsISO8601 = Clock.System.now(),
    val teacherInviteGuid: String? = null,
    val studentInviteGuid: String? = null,
    val permissions: List<ClassPermission> = listOf(
        ClassPermission(
            permissionToRef = ClassPermission.PermissionToEnrollmentRole(EnrollmentRoleEnum.TEACHER),
            permissions = PermissionFlags.CLASS_WRITE
        ),
        ClassPermission(
            permissionToRef = ClassPermission.PermissionToEnrollmentRole(EnrollmentRoleEnum.STUDENT),
            permissions = PermissionFlags.CLASS_READ
                .or(PermissionFlags.PERSON_TEACHER_READ)
                .or(PermissionFlags.PERSON_STUDENT_READ)
        ),
    )
): ModelWithTimes {

    companion object {

        const val TABLE_ID = 8

        const val DEFAULT_INVITE_CODE_MAX = 100_000
        const val DEFAULT_INVITE_CODE_LEN = 6
    }

}
