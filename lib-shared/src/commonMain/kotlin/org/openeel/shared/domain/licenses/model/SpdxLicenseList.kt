package world.respect.domain.licenses.model

import kotlinx.serialization.Serializable

@Serializable
data class SpdxLicenseList(
    val licenseListVersion: String,
    val licenses: List<SpdxLicense>
)