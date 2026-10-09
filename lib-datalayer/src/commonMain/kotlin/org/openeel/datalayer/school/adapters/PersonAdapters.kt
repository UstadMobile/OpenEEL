package world.respect.datalayer.school.adapters

import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.composites.PersonListDetails

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
