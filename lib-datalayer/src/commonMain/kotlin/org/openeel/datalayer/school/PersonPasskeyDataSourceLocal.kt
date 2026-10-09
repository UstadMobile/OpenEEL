package world.respect.datalayer.school

import world.respect.datalayer.school.model.PersonPasskey
import world.respect.datalayer.shared.LocalModelDataSource

interface PersonPasskeyDataSourceLocal: PersonPasskeyDataSource, LocalModelDataSource<PersonPasskey>

