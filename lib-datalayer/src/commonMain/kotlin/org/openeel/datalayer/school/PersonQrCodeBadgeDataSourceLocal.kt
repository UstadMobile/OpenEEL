package world.respect.datalayer.school

import world.respect.datalayer.school.model.PersonQrBadge
import world.respect.datalayer.shared.LocalModelDataSource

interface PersonQrCodeBadgeDataSourceLocal: PersonQrBadgeDataSource, LocalModelDataSource<PersonQrBadge>