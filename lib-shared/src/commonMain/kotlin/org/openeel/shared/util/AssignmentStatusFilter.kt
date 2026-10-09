package world.respect.shared.util

import org.jetbrains.compose.resources.StringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.all_students
import world.respect.shared.generated.resources.completed_status
import world.respect.shared.generated.resources.in_progress_status
import world.respect.shared.generated.resources.not_started_status

enum class AssignmentStatusFilter(val titleRes: StringResource) {
    ALL(Res.string.all_students),
    COMPLETED(Res.string.completed_status),
    IN_PROGRESS(Res.string.in_progress_status),
    NOT_STARTED(Res.string.not_started_status)
}
