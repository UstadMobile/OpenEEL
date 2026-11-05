package org.openeel.datalayer.school.model

import kotlinx.serialization.Serializable

@Serializable
data class ClazzUidAndName(
    var cGuid: Long = 0,
    var cTitle: String = ""
)