package world.respect.datalayer.school.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents class-based permissions
 *
 * @param permissionToRef who receives the permission
 * @param
 */
@Serializable
data class ClassPermission(
    val permissionToRef: ClassPermissionToRef,
    val permissions: Long,
) {

    //Use concrete properties as per
    // https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md#concrete-properties-in-a-base-class
    @Serializable
    sealed interface ClassPermissionToRef

    @Serializable
    @SerialName("enrollmentrole")
    data class PermissionToEnrollmentRole(val enrollmentRole: EnrollmentRoleEnum): ClassPermissionToRef

}

