package world.respect.shared.domain.license

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LicenseLabelEntry(
    val id: String,
    val name: String,
    @SerialName("spdx_id")
    val spdxId: String,
    val version: String = "",
    val approved: Boolean = false,
    @SerialName("license_steward_url")
    val licenseStewardUrl: String? = null,
    @SerialName("_links")
    val links: LicenseLabelLinks? = null,
)

@Serializable
data class LicenseLabelLinks(
    val self: LicenseLabelLink? = null,
    val html: LicenseLabelLink? = null,
    val collection: LicenseLabelLink? = null,
)

@Serializable
data class LicenseLabelLink(
    val href: String,
)

