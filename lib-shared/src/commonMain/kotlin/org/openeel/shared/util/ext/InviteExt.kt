package org.openeel.shared.util.ext

import org.jetbrains.compose.resources.StringResource
import org.openeel.datalayer.school.model.ClassInvite
import org.openeel.datalayer.school.model.ClassInviteModeEnum
import org.openeel.datalayer.school.model.FamilyMemberInvite
import org.openeel.datalayer.school.model.Invite2
import org.openeel.datalayer.school.model.NewUserInvite
import org.openeel.app.userdirectory.model.PersonRoleEnum

val Invite2.roleLabel: StringResource
    get() = when(this) {
        is NewUserInvite -> this.role.label

        is ClassInvite -> if(this.inviteMode == ClassInviteModeEnum.VIA_PARENT) {
            PersonRoleEnum.PARENT.label
        }else {
            this.role.label
        }

        is FamilyMemberInvite -> {
            PersonRoleEnum.PARENT.label
        }
    }
