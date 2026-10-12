package org.openeel.shared.domain.getdeviceinfo

import org.openeel.datalayer.school.model.DeviceInfo

interface GetDeviceInfoUseCase {

    operator fun invoke() : DeviceInfo

}
