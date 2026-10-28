package org.openeel.lib.directory.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject
import org.openeel.lib.serializers.InstantAsISO8601
import kotlin.time.Clock

data class Person2(
    val guid: String,
    val userActive: Boolean = true,
    val lastModified: InstantAsISO8601 = Clock.System.now(),
    val stored: InstantAsISO8601 = Clock.System.now(),
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
)