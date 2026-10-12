package org.openeel.datalayer.school

import org.openeel.datalayer.school.model.SchoolPermissionGrant
import org.openeel.datalayer.shared.LocalModelDataSource

interface SchoolPermissionGrantDataSourceLocal: SchoolPermissionGrantDataSource,
    LocalModelDataSource<SchoolPermissionGrant>
