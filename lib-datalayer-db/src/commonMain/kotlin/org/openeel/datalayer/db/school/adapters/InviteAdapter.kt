package org.openeel.datalayer.db.school.adapters

import org.openeel.datalayer.UidNumberMapper
import org.openeel.datalayer.db.school.entities.InviteEntity
import org.openeel.datalayer.school.model.ClassInvite
import org.openeel.datalayer.school.model.ClassInviteModeEnum
import org.openeel.datalayer.school.model.FamilyMemberInvite
import org.openeel.datalayer.school.model.Invite2
import org.openeel.datalayer.school.model.NewUserInvite

fun InviteEntity.toModel(): Invite2 {
    return when {
        iNewUserRole != null -> {
            NewUserInvite(
                uid = iGuid,
                code = iCode,
                approvalRequiredAfter = iApprovalRequiredAfter,
                lastModified = iLastModified,
                stored = iStored,
                status = iStatus,
                role = iNewUserRole,
                firstUser = iNewUserFirstInvite,
            )
        }

        iForClassGuid != null && iForClassRole != null-> {
            ClassInvite(
                uid = iGuid,
                code = iCode,
                approvalRequiredAfter = iApprovalRequiredAfter,
                lastModified = iLastModified,
                stored = iStored,
                status = iStatus,
                classUid = iForClassGuid,
                role = iForClassRole,
                inviteMode = iInviteMode ?: ClassInviteModeEnum.DIRECT,
            )
        }

        iForFamilyOfGuid != null-> {
            FamilyMemberInvite(
                uid = iGuid,
                code = iCode,
                approvalRequiredAfter = iApprovalRequiredAfter,
                lastModified = iLastModified,
                stored = iStored,
                status = iStatus,
                personUid = iForFamilyOfGuid
            )
        }

        else -> {
            throw IllegalArgumentException()
        }
    }
}

fun Invite2.toEntity(
    uidNumberMapper: UidNumberMapper,
): InviteEntity {
    val baseInviteEntity = InviteEntity(
        iGuid = uid,
        iGuidHash = uidNumberMapper(uid),
        iCode = code,
        iApprovalRequiredAfter = approvalRequiredAfter,
        iLastModified = lastModified,
        iStored = stored,
        iStatus =  status,
    )

    return when(this) {
        is NewUserInvite -> {
            baseInviteEntity.copy(
                iNewUserRole = role,
                iNewUserFirstInvite = firstUser,
            )
        }

        is ClassInvite -> {
            baseInviteEntity.copy(
                iForClassGuid = classUid,
                iForClassGuidHash = uidNumberMapper(classUid),
                iForClassRole = role,
                iInviteMode = inviteMode,
            )
        }

        is FamilyMemberInvite -> {
            baseInviteEntity.copy(
                iForFamilyOfGuid = personUid,
                iForFamilyOfGuidHash = uidNumberMapper(personUid),
            )
        }
    }
}
