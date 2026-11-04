package org.openeel.shared.domain.usagereporting

interface SetUsageReportingEnabledUseCase {

    operator fun invoke(enabled: Boolean)

}