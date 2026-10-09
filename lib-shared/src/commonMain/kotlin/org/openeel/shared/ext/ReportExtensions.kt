package world.respect.shared.ext

import org.jetbrains.compose.resources.StringResource
import world.respect.lib.xapi.extensions.reportoptions.Comparisons
import world.respect.lib.xapi.extensions.reportoptions.FilterType
import world.respect.lib.xapi.extensions.reportoptions.GenderType
import world.respect.lib.xapi.extensions.reportoptions.ReportPeriodOption
import world.respect.lib.xapi.extensions.reportoptions.ReportSeriesVisualType
import world.respect.lib.xapi.extensions.reportoptions.ReportTimeRangeUnit
import world.respect.lib.xapi.extensions.reportoptions.ReportXAxis
import world.respect.lib.xapi.extensions.reportoptions.YAxisTypes
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.*

val ReportTimeRangeUnit.label: StringResource
    get() = when(this) {
        ReportTimeRangeUnit.DAY -> Res.string.day
        ReportTimeRangeUnit.WEEK -> Res.string.weeks
        ReportTimeRangeUnit.MONTH -> Res.string.months
        ReportTimeRangeUnit.YEAR -> Res.string.year
    }

val ReportPeriodOption.label: StringResource
    get() = when(this) {
        ReportPeriodOption.LAST_WEEK -> Res.string.last_week
        ReportPeriodOption.LAST_30_DAYS -> Res.string.last_30_days
        ReportPeriodOption.LAST_3_MONTHS -> Res.string.last_3_months
        ReportPeriodOption.CUSTOM_PERIOD -> Res.string.custom_period
        ReportPeriodOption.CUSTOM_DATE_RANGE -> Res.string.custom_date_range
    }

val YAxisTypes.label: StringResource
    get() = when(this) {
        YAxisTypes.COUNT -> Res.string.count
        YAxisTypes.DURATION -> Res.string.duration
        YAxisTypes.PERCENTAGE -> Res.string.percentage
    }

val ReportSeriesVisualType.label: StringResource
    get() = when(this) {
        ReportSeriesVisualType.BAR_CHART -> Res.string.bar_chart
        ReportSeriesVisualType.LINE_GRAPH -> Res.string.line_chart
    }

val ReportXAxis.label: StringResource
    get() = when(this) {
        ReportXAxis.DAY -> Res.string.day
        ReportXAxis.WEEK -> Res.string.weekly
        ReportXAxis.MONTH -> Res.string.monthly
        ReportXAxis.QUARTER -> Res.string.quarterly
        ReportXAxis.YEAR -> Res.string.yearly
        ReportXAxis.TIME_OF_DAY -> Res.string.time_of_day
        ReportXAxis.CLASS -> Res.string.class_name
        ReportXAxis.SUBJECT -> Res.string.subject
        ReportXAxis.SCHOOL -> Res.string.school
        ReportXAxis.ASSESSMENT_TYPE -> Res.string.assessment_type
        ReportXAxis.GRADE_LEVEL -> Res.string.grade_level
        ReportXAxis.GENDER -> Res.string.gender
        ReportXAxis.AGE_GROUP -> Res.string.age_group
        ReportXAxis.REGION -> Res.string.region
        ReportXAxis.LANGUAGE -> Res.string.language
        ReportXAxis.USER_ROLE -> Res.string.user_role
        ReportXAxis.ACTIVITY_VERB -> Res.string.activity_verb
        ReportXAxis.APPLICATION -> Res.string.application
        ReportXAxis.DEVICE_TYPE -> Res.string.device_type
    }

val FilterType.label: StringResource
    get() = when(this) {
        FilterType.PERSON_AGE -> Res.string.person_age
        FilterType.PERSON_GENDER -> Res.string.person_gender
    }

val GenderType.label: StringResource
    get() = when(this) {
        GenderType.MALE -> Res.string.male
        GenderType.FEMALE -> Res.string.female
        GenderType.OTHER -> Res.string.other
    }

val Comparisons.label: StringResource
    get() = when(this) {
        Comparisons.EQUALS -> Res.string.equals
        Comparisons.NOT_EQUALS -> Res.string.not_equals
        Comparisons.GREATER -> Res.string.greater
        Comparisons.LESSER -> Res.string.lesser
        Comparisons.GREATER_OR_EQUAL -> Res.string.greater_or_equal
        Comparisons.LESSER_OR_EQUAL -> Res.string.lesser_or_equal
    }