package world.respect.datalayer.school

import world.respect.datalayer.school.model.PersonPassword
import world.respect.datalayer.shared.LocalModelDataSource

interface PersonPasswordDataSourceLocal: PersonPasswordDataSource, LocalModelDataSource<PersonPassword>
