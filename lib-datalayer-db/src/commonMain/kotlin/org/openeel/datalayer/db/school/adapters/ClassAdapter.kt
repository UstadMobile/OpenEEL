package world.respect.datalayer.db.school.adapters

import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.entities.ClassEntity
import world.respect.datalayer.db.school.entities.ClassEntityWithPermissions
import world.respect.datalayer.db.school.entities.ClassPermissionEntity
import world.respect.datalayer.school.model.ClassPermission
import world.respect.datalayer.school.model.Clazz

data class ClassEntities(
    val clazz: ClassEntity,
    val permissionEntities: List<ClassPermissionEntity> = emptyList(),
)

fun ClassEntityWithPermissions.toClassEntities(): ClassEntities = ClassEntities(
    clazz = clazz,
    permissionEntities = permissions,
)

fun ClassEntities.toModel(): Clazz {
    return Clazz(
        guid = clazz.cGuid,
        title = clazz.cTitle,
        status = clazz.cStatus,
        description = clazz.cDescription,
        lastModified = clazz.cLastModified,
        stored = clazz.cStored,
        teacherInviteGuid = clazz.cTeacherInviteGuid,
        studentInviteGuid = clazz.cStudentInviteGuid,
        permissions = permissionEntities.map { permissionEntity ->
            val toEnrollmentRole = permissionEntity.cpeToEnrollmentRole
            val toRef = when  {
                toEnrollmentRole != null -> ClassPermission.PermissionToEnrollmentRole(toEnrollmentRole)
                else -> throw IllegalArgumentException()
            }

            ClassPermission(
                permissionToRef = toRef,
                permissions = permissionEntity.cpePermissions,
            )
        }
    )
}


fun Clazz.toEntities(
    uidNumberMapper: UidNumberMapper,
): ClassEntities {
    val classUidNum = uidNumberMapper(guid)

    return ClassEntities(
        clazz = ClassEntity(
            cGuid = guid,
            cGuidHash = classUidNum,
            cTitle = title,
            cStatus = status,
            cDescription = description,
            cLastModified = lastModified,
            cStored = stored,
            cStudentInviteGuid = studentInviteGuid,
            cTeacherInviteGuid = teacherInviteGuid,
        ),
        permissionEntities = permissions.map {
            ClassPermissionEntity(
                cpeClassUidNum = classUidNum,
                cpeToEnrollmentRole = (it.permissionToRef as? ClassPermission.PermissionToEnrollmentRole)
                    ?.enrollmentRole,
                cpePermissions = it.permissions,
            )
        }
    )
}