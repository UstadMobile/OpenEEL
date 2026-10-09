package world.respect.shared.util

import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonRole
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.school.model.PersonStatusEnum
import world.respect.shared.domain.account.invite.RespectRedeemInviteRequest

fun RespectRedeemInviteRequest.PersonInfo.toPerson(
    role: PersonRoleEnum,
    username: String?=null,
    guid: String,
) : Person {
    return Person(
        guid =  guid,
        status = PersonStatusEnum.PENDING_APPROVAL,
        givenName = name.substringBeforeLast(" "),
        familyName = name.substringAfterLast(" "),
        username = username,
        gender = gender,
        roles = listOf(
            PersonRole(
                isPrimaryRole = true,
                roleEnum = role,
            )
        )
    )
}
