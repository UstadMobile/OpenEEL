package world.respect.shared.domain.license

import world.respect.shared.resources.UiText

interface GetLicenseLabelUseCase {
    data class LicenseLabelResult(
        val title: UiText,
        val isOpenSource: Boolean,
    )

    suspend operator fun invoke(licenseUrl: String): LicenseLabelResult
}