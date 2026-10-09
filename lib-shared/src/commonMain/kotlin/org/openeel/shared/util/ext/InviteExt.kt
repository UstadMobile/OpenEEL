package world.respect.shared.util.ext

import org.jetbrains.compose.resources.StringResource
import world.respect.datalayer.school.model.ClassInvite
import world.respect.datalayer.school.model.ClassInviteModeEnum
import world.respect.datalayer.school.model.FamilyMemberInvite
import world.respect.datalayer.school.model.Invite2
import world.respect.datalayer.school.model.NewUserInvite
import world.respect.datalayer.school.model.PersonRoleEnum

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
