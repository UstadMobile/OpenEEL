package org.openeel.shared.domain.account.invite

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.openeel.credentials.passkey.OpenEelCredential
import org.openeel.datalayer.school.model.DeviceInfo
import org.openeel.datalayer.school.model.Invite2
import org.openeel.datalayer.school.model.PersonGenderEnum

/**
 *
 */
@Serializable
data class RedeemInviteRequest(
    val code: String,
    val accountPersonInfo: PersonInfo,
    val account: Account,
    val deviceName: String? = null,
    val deviceInfo: DeviceInfo? = null,
    val invite: Invite2,
) {

    @Serializable
    data class PersonInfo(
       val name: String = "",
       val gender: PersonGenderEnum = PersonGenderEnum.UNSPECIFIED,
       val dateOfBirth: LocalDate = DATE_OF_BIRTH_EPOCH,
    )

    @Serializable
    data class Account(
        val guid: String,
        val username: String,
        val credential: OpenEelCredential,
    )

    companion object {

        val DATE_OF_BIRTH_EPOCH = LocalDate(1900, 1, 1)

    }

}