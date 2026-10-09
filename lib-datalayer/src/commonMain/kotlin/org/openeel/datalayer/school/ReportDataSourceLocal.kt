package world.respect.datalayer.school

import world.respect.datalayer.school.model.Report
import world.respect.datalayer.shared.LocalModelDataSource

interface ReportDataSourceLocal: ReportDataSource, LocalModelDataSource<Report>
