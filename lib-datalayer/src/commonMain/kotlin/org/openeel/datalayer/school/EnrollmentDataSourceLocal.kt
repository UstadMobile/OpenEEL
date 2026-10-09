package org.openeel.datalayer.school

import org.openeel.datalayer.school.model.Enrollment
import org.openeel.datalayer.shared.LocalModelDataSource

interface EnrollmentDataSourceLocal: EnrollmentDataSource, LocalModelDataSource<Enrollment>
