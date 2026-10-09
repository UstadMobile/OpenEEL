package world.respect.datalayer.school

import world.respect.datalayer.school.model.Clazz
import world.respect.datalayer.shared.LocalModelDataSource

interface ClassDataSourceLocal: ClassDataSource, LocalModelDataSource<Clazz>
