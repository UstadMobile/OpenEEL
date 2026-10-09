package world.respect.shared.domain.getdeviceinfo

import world.respect.datalayer.school.model.DeviceInfo

interface GetDeviceInfoUseCase {

    operator fun invoke() : DeviceInfo

}
