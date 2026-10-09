package world.respect.shared.domain.getdeviceinfo

import world.respect.datalayer.school.model.DeviceInfo

fun DeviceInfo.toUserFriendlyString(): String {
    return "${this.manufacturer ?: "Unknown Manufacturer"} ${this.model ?: "Unknown Model"}"
}
