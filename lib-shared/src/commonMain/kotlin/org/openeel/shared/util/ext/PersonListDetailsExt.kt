package org.openeel.shared.util.ext

import org.openeel.datalayer.school.model.composites.PersonListDetails

fun PersonListDetails.fullName() = "$givenName $familyName"
