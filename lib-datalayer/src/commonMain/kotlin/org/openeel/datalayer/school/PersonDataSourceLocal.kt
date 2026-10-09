package world.respect.datalayer.school

import world.respect.datalayer.school.model.Person
import world.respect.datalayer.shared.LocalModelDataSource

interface PersonDataSourceLocal: PersonDataSource, LocalModelDataSource<Person>
