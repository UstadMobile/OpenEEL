package org.openeel.datalayer.host.model

import kotlinx.serialization.Serializable

@Serializable
data class HostConfigSetting(
    val key: String,
    val value: String,
)
