package org.openeel.datalayer.school

import org.openeel.datalayer.school.model.PersonQrBadge
import org.openeel.datalayer.shared.LocalModelDataSource

interface PersonQrCodeBadgeDataSourceLocal: PersonQrBadgeDataSource, LocalModelDataSource<PersonQrBadge>