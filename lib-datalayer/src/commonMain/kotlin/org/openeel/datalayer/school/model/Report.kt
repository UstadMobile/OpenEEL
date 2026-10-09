package world.respect.datalayer.school.model

import kotlinx.serialization.Serializable
import world.respect.lib.xapi.extensions.reportoptions.ReportOptions
import world.respect.datalayer.shared.ModelWithTimes
import world.respect.lib.serializers.InstantAsISO8601
import kotlin.time.Clock

@Serializable
data class Report(
    val guid: String,
    val ownerGuid: String,
    val title: String,
    val reportOptions: ReportOptions,
    val reportIsTemplate: Boolean = false,
    val active: Boolean = true,
    override val lastModified: InstantAsISO8601 = Clock.System.now(),
    override val stored: InstantAsISO8601 = Clock.System.now(),
) : ModelWithTimes {

    companion object {
        const val TABLE_ID = 4
    }
}