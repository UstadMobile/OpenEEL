package org.openeel.datalayer.db.school.adapters

import kotlinx.serialization.json.Json
import org.openeel.datalayer.db.realm.entities.IndicatorEntity
import org.openeel.datalayer.db.school.entities.ReportEntity
import org.openeel.lib.xapi.extensions.reportoptions.ReportOptions
import org.openeel.lib.xapi.extensions.reportoptions.Indicator
import org.openeel.datalayer.school.model.Report
import kotlin.time.Instant

// Extension functions for conversion
fun ReportEntity.toRespectReport(): Report {
    return Report(
        guid = rGuid,
        ownerGuid = rOwnerGuid,
        title = rTitle,
        reportOptions = Json.decodeFromString(
            ReportOptions.serializer(), rOptions.trim()
        ),
        reportIsTemplate = rIsTemplate,
        active = rActive,
        stored = Instant.fromEpochMilliseconds(rStored),
        lastModified = Instant.fromEpochMilliseconds(rLastModified),
    )
}

fun Report.toReportEntity(): ReportEntity {
    return ReportEntity(
        rGuid = guid,
        rOwnerGuid = ownerGuid,
        rTitle = title,
        rOptions = Json.encodeToString(reportOptions),
        rIsTemplate = reportIsTemplate,
        rActive = active,
        rLastModified = lastModified.toEpochMilliseconds(),
        rStored = stored.toEpochMilliseconds(),
    )
}

fun IndicatorEntity.toIndicator(): Indicator {
    return Indicator(
        indicatorId = this.indicatorId,
        name = this.name,
        description = this.description,
        type = this.type,
        sql = this.sql
    )
}

fun Indicator.toIndicatorEntity(): IndicatorEntity {
    return IndicatorEntity(
        indicatorId = this.indicatorId,
        name = this.name,
        description = this.description,
        type = this.type,
        sql = this.sql
    )
}