package org.openeel.datalayer.school.ext

import io.ktor.http.Url
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.openeel.datalayer.exceptions.ForbiddenException
import org.openeel.datalayer.school.model.Invite2
import org.openeel.datalayer.school.model.Person
import org.openeel.datalayer.school.model.PersonRoleEnum
import org.openeel.lib.xapi.model.XapiAccount
import org.openeel.lib.xapi.model.XapiAgent

fun Person?.assertPersonHasRole(
    role: PersonRoleEnum
) {
    if(this?.roles?.any { it.roleEnum == role } != true) {
        throw ForbiddenException("Person does not have ${role.value} role")
    }
}

fun Person.primaryRoleOrNull(): PersonRoleEnum? {
    return roles.firstOrNull { it.isPrimaryRole }?.roleEnum
}

fun Person.primaryRole(): PersonRoleEnum {
    return roles.first { it.isPrimaryRole }.roleEnum
}

/**
 * Put the invite code in the metadata of the Person
 */
fun Person.copyWithInviteInfo(
    invite: Invite2
): Person {
    return copy(
        metadata = buildJsonObject {
            this@copyWithInviteInfo.metadata?.also {
                putAll(it)
            }

            put(Person.METADATA_KEY_INVITE_ID, JsonPrimitive(invite.code))
            put(Person.METADATA_KEY_INVITE_UID, JsonPrimitive(invite.uid))
        }
    )
}

fun Person.inviteCodeOrNull(): String? {
    return metadata?.get(Person.METADATA_KEY_INVITE_ID)?.jsonPrimitive?.contentOrNull
}

fun Person.inviteUidOrNull(): String? {
    return metadata?.get(Person.METADATA_KEY_INVITE_UID)?.jsonPrimitive?.contentOrNull
}

/**
 * Convention: accountUrl is now the school url. Eventually there will be no school url, only the
 * xapi URL.
 */
fun Person.asXapiAgent(
    accountUrl: Url
) : XapiAgent {
    return XapiAgent(
        name = "$givenName $familyName",
        account = XapiAccount(
            homePage = accountUrl.toString(),
            name = guid,
        )
    )
}
