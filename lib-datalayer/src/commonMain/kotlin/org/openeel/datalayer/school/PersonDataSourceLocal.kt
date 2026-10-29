package org.openeel.datalayer.school

import org.openeel.lib.directory.model.Person
import org.openeel.datalayer.shared.LocalModelDataSource

interface PersonDataSourceLocal: PersonDataSource, LocalModelDataSource<Person>
