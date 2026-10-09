package world.respect.datalayer.school

import world.respect.datalayer.school.model.SchoolPermissionGrant
import world.respect.datalayer.shared.LocalModelDataSource

interface SchoolPermissionGrantDataSourceLocal: SchoolPermissionGrantDataSource,
    LocalModelDataSource<SchoolPermissionGrant>
