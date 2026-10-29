package org.openeel.shared.util

import org.openeel.lib.directory.model.Person
import org.openeel.lib.directory.model.PersonRole
import org.openeel.lib.directory.model.PersonRoleEnum
import org.openeel.datalayer.school.model.PersonStatusEnum
import org.openeel.shared.domain.account.invite.RedeemInviteRequest

fun RedeemInviteRequest.PersonInfo.toPerson(
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
