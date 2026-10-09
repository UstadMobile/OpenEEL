package world.respect.datalayer.school.ext

import io.ktor.http.Url
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import world.respect.datalayer.exceptions.ForbiddenException
import world.respect.datalayer.school.model.DeviceInfo
import world.respect.datalayer.school.model.Invite2
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.lib.xapi.model.XapiAccount
import world.respect.lib.xapi.model.XapiAgent

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
    invite: Invite2,
    deviceInfo: DeviceInfo? = null
): Person {
    return copy(
        metadata = buildJsonObject {
            this@copyWithInviteInfo.metadata?.also {
                putAll(it)
            }

            put(Person.METADATA_KEY_INVITE_ID, JsonPrimitive(invite.code))
            put(Person.METADATA_KEY_INVITE_UID, JsonPrimitive(invite.uid))

            if (deviceInfo != null) {
                put(Person.DEVICE_INFO, JsonPrimitive(deviceInfo.toString()))
                put(Person.DEVICE_MODEL, JsonPrimitive(deviceInfo.model))
                put(Person.DEVICE_PLATFORM, JsonPrimitive(deviceInfo.platform.name))
                put(Person.DEVICE_OS_VERSION, JsonPrimitive(deviceInfo.version))
            }
        }
    )
}

fun Person.inviteCodeOrNull(): String? {
    return metadata?.get(Person.METADATA_KEY_INVITE_ID)?.jsonPrimitive?.contentOrNull
}

fun Person.inviteUidOrNull(): String? {
    return metadata?.get(Person.METADATA_KEY_INVITE_UID)?.jsonPrimitive?.contentOrNull
}
fun Person.deviceModelOrNull(): String? {
    return metadata?.jsonObject?.get(Person.DEVICE_MODEL)?.jsonPrimitive?.content
}

fun Person.devicePlatformOrNull(): String? {
    return metadata?.jsonObject?.get(Person.DEVICE_PLATFORM)?.jsonPrimitive?.content
}

fun Person.deviceOsVersionOrNull(): String? {
    return metadata?.jsonObject?.get(Person.DEVICE_OS_VERSION)?.jsonPrimitive?.content
}


fun Person.getDeviceInfo(): String {
    val model = deviceModelOrNull() ?: return givenName
    val platform = devicePlatformOrNull() ?: "Android"
    val osVersion = deviceOsVersionOrNull() ?: ""

    val deviceType = if (model.contains("tab", ignoreCase = true) ||
        model.contains("pad", ignoreCase = true)) "Tablet" else "Mobile"

    return "$deviceType ($platform $osVersion)"
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
