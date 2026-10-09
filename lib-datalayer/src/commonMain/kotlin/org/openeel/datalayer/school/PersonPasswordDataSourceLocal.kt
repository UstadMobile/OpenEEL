package org.openeel.datalayer.school

import org.openeel.datalayer.school.model.PersonPassword
import org.openeel.datalayer.shared.LocalModelDataSource

interface PersonPasswordDataSourceLocal: PersonPasswordDataSource, LocalModelDataSource<PersonPassword>
