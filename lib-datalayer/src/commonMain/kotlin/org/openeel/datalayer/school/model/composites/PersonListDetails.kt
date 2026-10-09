package world.respect.datalayer.school.model.composites

import world.respect.datalayer.school.model.PersonRoleEnum


/**
 * Data class for the details shown in the list (fewer fields than full entity; exists for performance
 * reasons)
 */
data class PersonListDetails(
    val guid: String,
    val givenName: String,
    val familyName: String,
    val username: String?,
    val email: String?,
    val phoneNumber: String?,
    val role: PersonRoleEnum?,
)
