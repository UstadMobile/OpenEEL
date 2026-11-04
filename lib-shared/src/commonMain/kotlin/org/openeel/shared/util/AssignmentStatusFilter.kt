package org.openeel.shared.util

import org.jetbrains.compose.resources.StringResource
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.all_students
import org.openeel.shared.generated.resources.completed_status
import org.openeel.shared.generated.resources.in_progress_status
import org.openeel.shared.generated.resources.not_started_status

enum class AssignmentStatusFilter(val titleRes: StringResource) {
    ALL(Res.string.all_students),
    COMPLETED(Res.string.completed_status),
    IN_PROGRESS(Res.string.in_progress_status),
    NOT_STARTED(Res.string.not_started_status)
}
