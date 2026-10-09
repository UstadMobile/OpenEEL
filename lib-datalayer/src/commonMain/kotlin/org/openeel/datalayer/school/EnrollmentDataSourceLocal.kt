package world.respect.datalayer.school

import world.respect.datalayer.school.model.Enrollment
import world.respect.datalayer.shared.LocalModelDataSource

interface EnrollmentDataSourceLocal: EnrollmentDataSource, LocalModelDataSource<Enrollment>
