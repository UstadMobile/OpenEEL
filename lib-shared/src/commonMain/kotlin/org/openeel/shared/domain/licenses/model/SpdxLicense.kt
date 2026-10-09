package world.respect.domain.licenses.model

import kotlinx.serialization.Serializable

@Serializable
data class SpdxLicense(
    val reference: String,
    val isDeprecatedLicenseId: Boolean,
    val detailsUrl: String,
    val referenceNumber: Int,
    val name: String,
    val licenseId: String,
    val seeAlso: List<String>,
    val isOsiApproved: Boolean,
    val isFsfLibre: Boolean? = null,
    val licenseText: String? = null,
    val standardLicenseHeader: String? = null,
)