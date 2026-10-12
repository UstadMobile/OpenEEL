package org.openeel.shared.util.ext

import org.openeel.shared.domain.account.UserAccount

/**
 * Returns true if the two accounts are the same account - the same user guid on the same school
 */
fun UserAccount.isSameAccount(other: UserAccount): Boolean {
    return other.userGuid == userGuid && other.school.self == school.self
}
