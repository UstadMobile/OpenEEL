package org.openeel.datalayer.school

import org.openeel.datalayer.school.model.PersonPasskey
import org.openeel.datalayer.shared.LocalModelDataSource

interface PersonPasskeyDataSourceLocal: PersonPasskeyDataSource, LocalModelDataSource<PersonPasskey>

