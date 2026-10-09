package org.openeel.app.userdirectory.model

import kotlinx.serialization.json.JsonObject
import org.openeel.datalayer.school.model.PersonGenderEnum
import java.time.LocalDate

class Person2(
    val guid: String,
    val actorIfis: List<String> = emptyList(),
    val userActive: Boolean = true,
    val metadata: JsonObject? = null,
    val userMasterIdentifier: String? = null,
    val username: String? = null,
    val givenName: String,
    val familyName: String,
    val middleName: String? = null,
    val gender: PersonGenderEnum,
    val preferredFirstName: String? = null,
    val preferredMiddleName: String? = null,
    val preferredLastName: String? = null,
    val pronouns: String? = null,
    val roles: List<PersonRole>,
    val relatedPersonUids: List<String> = emptyList(),
    val dateOfBirth: LocalDate? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
) {
}