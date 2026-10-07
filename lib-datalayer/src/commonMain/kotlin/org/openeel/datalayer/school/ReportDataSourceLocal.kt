package org.openeel.datalayer.school

import org.openeel.datalayer.school.model.Report
import org.openeel.datalayer.shared.LocalModelDataSource

interface ReportDataSourceLocal: ReportDataSource, LocalModelDataSource<Report>
