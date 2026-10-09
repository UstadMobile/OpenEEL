package world.respect.shared.domain.usagereporting

/**
 * Indicates if anonymous reporting and crash reporting is enabled.
 */
interface GetUsageReportingEnabledUseCase {

    operator fun invoke(): Boolean

}