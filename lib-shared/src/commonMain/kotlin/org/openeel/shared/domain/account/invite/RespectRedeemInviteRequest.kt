package world.respect.shared.domain.account.invite

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import world.respect.credentials.passkey.RespectCredential
import world.respect.datalayer.school.model.DeviceInfo
import world.respect.datalayer.school.model.Invite2
import world.respect.datalayer.school.model.PersonGenderEnum

/**
 *
 */
@Serializable
data class RespectRedeemInviteRequest(
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

    /**
     * @param userHandleEncoded the base64 encoded user handle, as would be used with a passkey,
     *        as per RespectUserHandle.
     */
    @Serializable
    data class Account(
        val guid: String,
        val username: String,
        val credential: RespectCredential? = null,
    )

    companion object {

        val DATE_OF_BIRTH_EPOCH = LocalDate(1900, 1, 1)

    }

}