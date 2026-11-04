package org.openeel.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.openeel.datalayer.school.model.EnrollmentRoleEnum

@Entity
data class ClassPermissionEntity(
    @PrimaryKey(autoGenerate = true)
    val cpeId: Long = 0,
    val cpeClassUidNum: Long = 0,
    val cpeToEnrollmentRole: EnrollmentRoleEnum? = null,
    val cpePermissions: Long,
)

