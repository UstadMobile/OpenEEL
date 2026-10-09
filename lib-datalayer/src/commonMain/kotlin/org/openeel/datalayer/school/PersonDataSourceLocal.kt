package org.openeel.datalayer.school

import org.openeel.datalayer.school.model.Person
import org.openeel.datalayer.shared.LocalModelDataSource

interface PersonDataSourceLocal: PersonDataSource, LocalModelDataSource<Person>
