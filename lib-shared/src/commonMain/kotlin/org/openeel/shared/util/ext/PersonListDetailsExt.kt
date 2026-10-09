package world.respect.shared.util.ext

import world.respect.datalayer.school.model.composites.PersonListDetails

fun PersonListDetails.fullName() = "$givenName $familyName"
