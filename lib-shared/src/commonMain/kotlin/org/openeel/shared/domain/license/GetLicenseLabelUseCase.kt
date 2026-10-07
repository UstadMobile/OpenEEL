package org.openeel.shared.domain.license

import org.openeel.shared.resources.UiText

interface GetLicenseLabelUseCase {
    data class LicenseLabelResult(
        val title: UiText,
        val isOpenSource: Boolean,
    )

    suspend operator fun invoke(licenseUrl: String): LicenseLabelResult
}