package org.openeel.datalayer.school.adapters

import org.openeel.datalayer.school.model.Person
import org.openeel.datalayer.school.model.composites.PersonListDetails

fun Person.asListDetails(): PersonListDetails {
    return PersonListDetails(
        guid = guid,
        givenName = givenName,
        familyName = familyName,
        username = username,
        email = email,
        phoneNumber = phoneNumber,
        role = roles.firstOrNull { it.isPrimaryRole }?.roleEnum,
    )
}
