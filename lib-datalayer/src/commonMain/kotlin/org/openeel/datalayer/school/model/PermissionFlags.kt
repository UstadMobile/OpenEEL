package world.respect.datalayer.school.model

@Suppress("unused")
object PermissionFlags {

    const val CLASS_READ = 1L

    const val CLASS_WRITE = 2L

    const val PERSON_STUDENT_READ = 4L

    const val PERSON_STUDENT_WRITE = 8L

    const val PERSON_PARENT_READ = 16L

    const val PERSON_PARENT_WRITE = 32L

    const val PERSON_TEACHER_READ = 64L

    /**
     * Permission to modify teachers, or add new users with the teacher role.
     */
    const val PERSON_TEACHER_WRITE = 128L

    //Note: writing an admin always requires admin permission
    const val PERSON_ADMIN_READ = 256L


    /**
     * When used as a SchoolPermission, then that role will have permission to add any student user
     * in the school to any class in the school.
     */
    const val CLASS_WRITE_STUDENT_ENROLLMENT = 512L

    const val CLASS_WRITE_TEACHER_ENROLLMENT = 1024L

    const val INVITE_NEW_USER = 2048L

    const val PERSON_ADMIN_WRITE = 4096L


    const val TEACHER_DEFAULT_SCHOOL_PERMISSIONS = CLASS_READ
            .or(CLASS_WRITE)
            .or(PERSON_STUDENT_READ)
            .or(PERSON_STUDENT_WRITE)
            .or(PERSON_PARENT_READ)
            .or(PERSON_PARENT_WRITE)
            .or(PERSON_TEACHER_READ)
            .or(CLASS_WRITE_STUDENT_ENROLLMENT)
            .or(CLASS_WRITE_TEACHER_ENROLLMENT)
            .or(INVITE_NEW_USER)

    const val STUDENT_DEFAULT_SCHOOL_PERMISSIONS = 0L

    const val PARENT_DEFAULT_SCHOOL_PERMISSIONS = 0L

    const val SYSADMIN_DEFAULT_SCHOOL_PERMISSIONS = Long.MAX_VALUE

    const val SHARED_DEVICE_DEFAULT_SCHOOL_PERMISSIONS = CLASS_READ
        .or(PERSON_STUDENT_READ)

}