package org.openeel.shared.util.ext

import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.StringResource
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.monday
import org.openeel.shared.generated.resources.tuesday
import org.openeel.shared.generated.resources.wednesday
import org.openeel.shared.generated.resources.thursday
import org.openeel.shared.generated.resources.friday
import org.openeel.shared.generated.resources.saturday
import org.openeel.shared.generated.resources.sunday

val DayOfWeek.dayStringResource: StringResource
    get() = when(this) {
        DayOfWeek.MONDAY -> Res.string.monday
        DayOfWeek.TUESDAY -> Res.string.tuesday
        DayOfWeek.WEDNESDAY -> Res.string.wednesday
        DayOfWeek.THURSDAY -> Res.string.thursday
        DayOfWeek.FRIDAY -> Res.string.friday
        DayOfWeek.SATURDAY -> Res.string.saturday
        DayOfWeek.SUNDAY -> Res.string.sunday
    }
