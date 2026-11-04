package org.openeel.datalayer.school

import org.openeel.datalayer.school.model.Clazz
import org.openeel.datalayer.shared.LocalModelDataSource

interface ClassDataSourceLocal: ClassDataSource, LocalModelDataSource<Clazz>
