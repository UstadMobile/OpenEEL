package world.respect.datalayer.respect.model.invite

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AuthOptionConfigTypeEnum(val value: String) {

    @SerialName("builtin")
    BUILTIN("builtin"),

    @SerialName("openid")
    OPENID("openid"),
}