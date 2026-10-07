package org.openeel.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.openeel.datalayer.school.model.ClassInviteModeEnum
import org.openeel.datalayer.school.model.EnrollmentRoleEnum
import org.openeel.datalayer.school.model.PersonRoleEnum
import org.openeel.datalayer.school.model.StatusEnum
import kotlin.time.Clock
import kotlin.time.Instant

@Entity
data class InviteEntity(
    val iGuid: String,
    @PrimaryKey
    val iGuidHash: Long,
    val iCode: String,
    val iApprovalRequiredAfter: Instant,
    val iLastModified: Instant = Clock.System.now(),
    val iStored: Instant = Clock.System.now(),
    val iStatus: StatusEnum,

    val iNewUserRole: PersonRoleEnum? = null,
    val iNewUserFirstInvite: Boolean = false,
    val iForFamilyOfGuid: String? = null,
    val iForFamilyOfGuidHash: Long? = null,
    val iForClassGuid: String? = null,
    val iForClassName: String? = null,
    val iInviteMode: ClassInviteModeEnum? = null,
    val iSchoolName: String? = null,
    val iForClassGuidHash: Long? = null,
    val iForClassRole: EnrollmentRoleEnum ?=null,
)