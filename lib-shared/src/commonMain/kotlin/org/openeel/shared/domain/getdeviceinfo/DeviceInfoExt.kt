package org.openeel.shared.domain.getdeviceinfo

import org.openeel.datalayer.school.model.DeviceInfo

fun DeviceInfo.toUserFriendlyString(): String {
    return "${this.manufacturer ?: "Unknown Manufacturer"} ${this.model ?: "Unknown Model"}"
}
