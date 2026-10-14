package org.openeel.datalayer.respect.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthenticationOption(
    val name: String,
    val provider: AuthOptionConfig,
) {
    companion object {
        const val BUILTIN_DEFAULT_NAME = "Builtin-Default"

        val BUILTIN_DEFAULT_OPTIONS = listOf(
            AuthenticationOption(
                name = BUILTIN_DEFAULT_NAME,
                provider = BuiltinAuthOptionConfig(),
            )
        )
    }
}
