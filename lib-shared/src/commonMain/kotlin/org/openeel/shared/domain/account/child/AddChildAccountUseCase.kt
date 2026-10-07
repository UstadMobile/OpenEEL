package org.openeel.shared.domain.account.child

import kotlinx.serialization.Serializable
import org.openeel.datalayer.school.model.Person
import org.openeel.shared.domain.account.invite.RespectRedeemInviteRequest

interface AddChildAccountUseCase {

    @Serializable
    data class AddChildAccountRequest(
        val childPersonInfo: RespectRedeemInviteRequest.PersonInfo,
        val parentUid: String,
        val inviteRedeemRequest: RespectRedeemInviteRequest
    )

    @Serializable
    data class AddChildAccountResponse(
        val childPerson: Person,
        val parentPerson: Person,
    )

     suspend operator fun invoke(
         request: AddChildAccountRequest,
    ): AddChildAccountResponse

     companion object {

         const val ENDPOINT_NAME = "addchild"

     }

}
