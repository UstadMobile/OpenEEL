package world.respect.shared.util.ext

import world.respect.shared.domain.account.RespectAccount

/**
 * Returns true if the two accounts are the same account - the same user guid on the same school
 */
fun RespectAccount.isSameAccount(other: RespectAccount): Boolean {
    return other.userGuid == userGuid && other.school.self == school.self
}
