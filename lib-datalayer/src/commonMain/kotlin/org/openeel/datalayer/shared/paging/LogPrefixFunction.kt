package world.respect.datalayer.shared.paging

/**
 * Alias for a function that is used to provide extra information that is prefixed in logs. This is
 * expected to remain constant.
 *
 * It is provided as a function instead of a string such that it need not be executed when fine/debug
 * level logging is not in use.
 */
typealias LogPrefixFunction = () -> String
