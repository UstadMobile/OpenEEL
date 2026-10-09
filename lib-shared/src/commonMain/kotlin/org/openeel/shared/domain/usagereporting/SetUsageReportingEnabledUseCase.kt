package world.respect.shared.domain.usagereporting

interface SetUsageReportingEnabledUseCase {

    operator fun invoke(enabled: Boolean)

}