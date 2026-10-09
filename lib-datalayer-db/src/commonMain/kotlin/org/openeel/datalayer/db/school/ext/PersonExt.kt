package world.respect.datalayer.db.school.ext

import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonRoleEnum

fun Person.fullName(): String = buildString {
    append(givenName)
    append(" ")
    middleName?.also {
        append(it)
        append(" ")
    }
    append(familyName)
}

fun Person.isAdminOrTeacher() : Boolean {
    return roles.any {
        it.roleEnum == PersonRoleEnum.SYSTEM_ADMINISTRATOR ||
                it.roleEnum == PersonRoleEnum.TEACHER
    }
}

fun Person.isStudent() : Boolean {
    return roles.any {
        it.roleEnum == PersonRoleEnum.STUDENT
    }
}

fun Person.isParent() : Boolean {
    return roles.any {
        it.roleEnum == PersonRoleEnum.PARENT
    }
}

fun Person.isTeacher(): Boolean {
    return roles.any {
        it.roleEnum == PersonRoleEnum.TEACHER
    }
}


fun Person.isAdmin() : Boolean {
    return roles.any {
        it.roleEnum == PersonRoleEnum.SYSTEM_ADMINISTRATOR
    }
}

fun Person.canAdminAccountFor(
    other: Person
): Boolean {
    return isAdmin() || (isAdminOrTeacher() && !other.isAdminOrTeacher())
}

